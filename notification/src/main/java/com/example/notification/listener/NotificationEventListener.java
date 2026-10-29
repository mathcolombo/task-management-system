package com.example.notification.listener;

import com.example.task.event.TaskCreatedEvent;
import com.example.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${rabbitmq.queue.notification}")
    public void handleTaskCreated(TaskCreatedEvent event) {
        log.info("📩 Notification Service recebeu TASK_CREATED: ID {}", event.getTaskId());
        notificationService.sendNotification(
                event.getTaskId(),
                event.getTitle(),
                event.getAssignee(),
                event.getPriority()
        );
    }
}
