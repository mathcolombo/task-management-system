package com.example.task.service;

import com.example.task.event.TaskCreatedEvent;
import com.example.task.model.Task;
import com.example.task.model.TaskStatus;
import com.example.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.created}")
    private String createdRoutingKey;

    @Transactional
    public Task createTask(Task task) {
        if (task.getTitle() == null || task.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Título da tarefa é obrigatório");
        }
        task.setStatus(TaskStatus.PENDING);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        Task savedTask = taskRepository.save(task);
        log.info("✅ Tarefa criada: ID {} - {}", savedTask.getId(), savedTask.getTitle());

        TaskCreatedEvent event = new TaskCreatedEvent(
                savedTask.getId(),
                savedTask.getTitle(),
                savedTask.getDescription(),
                savedTask.getAssignee(),
                savedTask.getPriority().name()
        );
        rabbitTemplate.convertAndSend(exchangeName, createdRoutingKey, event);
        log.info("📤 Evento TASK_CREATED publicado para tarefa {}", savedTask.getId());

        return savedTask;
    }
}
