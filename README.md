# Sistema de Gestão de Tarefas — EDA com Spring Boot e RabbitMQ

Sistema de gestão de tarefas construído com **Arquitetura Orientada a Eventos (EDA)**. Quando uma tarefa é criada, o serviço de tarefas publica um evento `TASK_CREATED` no RabbitMQ. Dois serviços independentes consomem esse evento: um envia notificações (simuladas) e o outro registra métricas.

## Arquitetura

```
                    POST /api/tasks
                          │
                ┌─────────▼──────────┐
                │    task-service    │  salva a tarefa no H2 e
                │      (8081)        │  publica TASK_CREATED
                └─────────┬──────────┘
                          │ routing key: task.created
                ┌─────────▼──────────┐
                │      RabbitMQ      │
                │ task.exchange      │  (Topic Exchange)
                └────┬──────────┬────┘
     notification.queue        analytics.queue
                     │          │
   ┌─────────────────▼──┐   ┌───▼─────────────────┐
   │notification-service│   │  analytics-service  │
   │      (8082)        │   │       (8083)        │
   │ envia notificação  │   │ contabiliza tarefas │
   │ (EMAIL / SMS)      │   │ por prioridade/dia  │
   └────────────────────┘   └─────────────────────┘
```

| Serviço | Porta | Papel | Banco H2 |
|---|---|---|---|
| `task` | 8081 | Produtor: cria tarefas e publica `TASK_CREATED` | `./data/taskdb` |
| `notification` | 8082 | Consumidor: registra notificações (canal **SMS** para prioridade `CRITICAL`, **EMAIL** para as demais) | `./data/notificationdb` |
| `analytics` | 8083 | Consumidor: mantém contagem diária de tarefas por prioridade | `./data/analyticsdb` |

Cada serviço tem seu próprio banco. Os serviços se comunicam só por eventos, sem chamadas diretas entre eles.

## Tecnologias

- Java 21
- Spring Boot 4.1 (Web MVC, Data JPA, AMQP)
- RabbitMQ 3 (via Docker)
- H2 Database (em arquivo)
- Lombok
- Maven (com Maven Wrapper)

## Estrutura do projeto

```
task-management-system/
├── docker-compose.yml          # RabbitMQ com Management UI
├── task/                       # task-service (porta 8081)
│   └── src/main/java/com/example/
│       ├── task/
│       │   ├── config/         # RabbitMQConfig (exchange + conversor JSON)
│       │   ├── controller/     # TaskController (/api/tasks)
│       │   ├── event/          # TaskEvent, TaskCreatedEvent
│       │   ├── model/          # Task, TaskPriority, TaskStatus
│       │   ├── repository/
│       │   └── service/        # TaskService (salva e publica o evento)
├── notification/               # notification-service (porta 8082)
│   └── src/main/java/com/example/
│       ├── notification/       # config, controller, listener, model, repository, service
│       └── task/event/         # cópia dos eventos (mesmo pacote do produtor)
└── analytics/                  # analytics-service (porta 8083)
    └── src/main/java/com/example/
        ├── analytics/          # config, controller, listener, model, repository, service
        └── task/event/         # cópia dos eventos (mesmo pacote do produtor)
```

> As classes de evento são copiadas para cada serviço no mesmo pacote `com.example.task.event`. Em um projeto real, ficariam em um módulo compartilhado.

## Pré-requisitos

- Docker (Docker Desktop no Windows/macOS)
- JDK 21

## Como executar

### 1. Subir o RabbitMQ

```bash
docker compose up -d
docker compose ps        # o container task-rabbitmq deve estar "healthy"
```

Interface de gerenciamento: http://localhost:15672 (login `admin` / `admin`).

### 2. Subir os serviços

Abra um terminal para cada serviço. **Inicie os consumidores antes do produtor**, porque são eles que declaram as filas:

```bash
# Terminal 1
cd analytics && ./mvnw spring-boot:run

# Terminal 2
cd notification && ./mvnw spring-boot:run

# Terminal 3
cd task && ./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd spring-boot:run`. Cada serviço está pronto quando o log mostra `Started ...Application`.

## Endpoints

| Serviço | Método | Endpoint | Descrição |
|---|---|---|---|
| task | `POST` | `/api/tasks` | Cria uma tarefa e publica `TASK_CREATED` |
| task | `GET` | `/api/tasks` | Lista todas as tarefas |
| task | `GET` | `/api/tasks/{id}` | Busca uma tarefa por ID (404 se não existir) |
| notification | `GET` | `/api/notifications` | Lista as notificações registradas |
| analytics | `GET` | `/api/metrics` | Lista as métricas diárias |

### Corpo da criação de tarefa

```json
{
  "title": "Implementar EDA",
  "description": "Criar sistema de tarefas com RabbitMQ",
  "assignee": "João Silva",
  "priority": "HIGH"
}
```

- `title` é obrigatório.
- `priority` aceita `LOW`, `MEDIUM`, `HIGH` ou `CRITICAL`.
- `status` (`PENDING`), `createdAt` e `updatedAt` são preenchidos pelo serviço.

## Testando a aplicação

### 1. Criar tarefas (uma de cada prioridade)

```bash
# HIGH → notificação por EMAIL
curl -X POST http://localhost:8081/api/tasks -H "Content-Type: application/json" \
  -d '{"title":"Implementar EDA","description":"Criar sistema de tarefas com RabbitMQ","assignee":"João Silva","priority":"HIGH"}'

# CRITICAL → notificação por SMS
curl -X POST http://localhost:8081/api/tasks -H "Content-Type: application/json" \
  -d '{"title":"Corrigir bug em produção","description":"Erro 500 no checkout","assignee":"Maria Souza","priority":"CRITICAL"}'

# MEDIUM
curl -X POST http://localhost:8081/api/tasks -H "Content-Type: application/json" \
  -d '{"title":"Revisar PR","description":"Revisar PR do módulo de login","assignee":"Carlos Lima","priority":"MEDIUM"}'

# LOW
curl -X POST http://localhost:8081/api/tasks -H "Content-Type: application/json" \
  -d '{"title":"Atualizar README","description":"Documentar execução local","assignee":"Ana Costa","priority":"LOW"}'
```

Resposta esperada (`201 Created`):

```json
{
  "id": 1,
  "title": "Implementar EDA",
  "description": "Criar sistema de tarefas com RabbitMQ",
  "assignee": "João Silva",
  "priority": "HIGH",
  "status": "PENDING",
  "createdAt": "2026-10-29T11:10:00.924496",
  "updatedAt": "2026-10-29T11:10:00.924527"
}
```

### 2. Verificar os logs

**task:**
```
✅ Tarefa criada: ID 1 - Implementar EDA
📤 Evento TASK_CREATED publicado para tarefa 1
```

**notification:**
```
📩 Notification Service recebeu TASK_CREATED: ID 1
📧 [NOTIFICAÇÃO] Canal: EMAIL | Mensagem: 📋 Nova tarefa: 'Implementar EDA' (Prioridade: HIGH) para João Silva
✅ Notificação registrada para tarefa 1
```

**analytics:**
```
📩 Analytics Service recebeu TASK_CREATED: ID 1
📊 Métrica atualizada: 2026-10-29 | Total: 1 | Críticas: null
```

> Os contadores de prioridade começam nulos e só recebem valor quando chega a primeira tarefa daquela prioridade. Por isso aparece `Críticas: null` antes da primeira tarefa `CRITICAL`.

### 3. Consultar os dados

```bash
curl http://localhost:8081/api/tasks
curl http://localhost:8081/api/tasks/1
curl http://localhost:8081/api/tasks/999          # 404 Not Found
curl http://localhost:8082/api/notifications
curl http://localhost:8083/api/metrics
```

Depois das 4 tarefas acima, `/api/metrics` retorna algo como:

```json
[{"id":1,"date":"2026-10-29","totalTasks":4,"highPriorityTasks":1,"mediumPriorityTasks":1,"lowPriorityTasks":1,"criticalPriorityTasks":1}]
```

### 4. Conferir no RabbitMQ Management

Em http://localhost:15672:

- **Exchanges:** existe `task.exchange`, do tipo `topic`.
- **Queues:** `notification.queue` e `analytics.queue`, cada uma com 1 consumer e `Ready = 0`.
- **Bindings** de cada fila: routing key `task.created`.

### 5. Cenários extras

**Validação de título.** Uma tarefa sem título é rejeitada (erro 500) e nenhum evento é publicado:

```bash
curl -i -X POST http://localhost:8081/api/tasks -H "Content-Type: application/json" \
  -d '{"title":"","assignee":"João","priority":"HIGH"}'
```

**Consumidor fora do ar.** O RabbitMQ guarda as mensagens até o consumidor voltar:

1. Pare o `notification` (Ctrl+C).
2. Crie 2 tarefas.
3. No Management, `notification.queue` mostra `Ready = 2`. O `analytics` continua processando normalmente.
4. Suba o `notification` de novo: ele consome as mensagens pendentes e a fila volta a `Ready = 0`.

### 6. Console do H2

| Serviço | URL | JDBC URL |
|---|---|---|
| task | http://localhost:8081/h2-console | `jdbc:h2:file:./data/taskdb` |
| notification | http://localhost:8082/h2-console | `jdbc:h2:file:./data/notificationdb` |
| analytics | http://localhost:8083/h2-console | `jdbc:h2:file:./data/analyticsdb` |

Usuário `sa`, senha em branco.

## Limpar os dados

O caminho dos bancos é relativo, então a pasta `data/` é criada dentro do diretório de onde cada serviço foi iniciado. Para recomeçar do zero, pare os serviços e rode:

```bash
rm -rf task/data notification/data analytics/data
```

Para parar o RabbitMQ (adicione `-v` para apagar também o volume com as filas):

```bash
docker compose down
```

## Problemas comuns

| Problema | Causa provável | Solução |
|---|---|---|
| `Connection refused` ao iniciar | RabbitMQ não está rodando | `docker compose up -d` e verificar com `docker compose ps` |
| Mensagem não é consumida | Fila sem binding ou consumidor parado | Conferir os bindings no Management e se o consumidor está no ar |
| Erro de desserialização no consumidor | Classe de evento em pacote diferente | Manter `TaskCreatedEvent` em `com.example.task.event` em todos os serviços |
| `Port already in use` | Outro processo usando a porta | Encerrar o processo ou alterar `server.port` no `application.properties` |

## Observação sobre Spring Boot 4

O guia original usa `Jackson2JsonMessageConverter`. Como este projeto usa Spring Boot 4, que traz o Jackson 3, o conversor usado nos três `RabbitMQConfig` é o `JacksonJsonMessageConverter`, equivalente no Spring AMQP 4.
