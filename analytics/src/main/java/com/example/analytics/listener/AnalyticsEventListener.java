package com.example.analytics.listener;

import com.example.task.event.TaskCreatedEvent;
import com.example.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsEventListener {

    private final AnalyticsService analyticsService;

    @RabbitListener(queues = "${rabbitmq.queue.analytics}")
    public void handleTaskCreated(TaskCreatedEvent event) {
        log.info("📩 Analytics Service recebeu TASK_CREATED: ID {}", event.getTaskId());
        analyticsService.recordTaskCreated(event.getPriority());
    }
}
