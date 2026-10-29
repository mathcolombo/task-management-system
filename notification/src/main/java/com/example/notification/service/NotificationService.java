package com.example.notification.service;

import com.example.notification.model.Notification;
import com.example.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void sendNotification(Long taskId, String taskTitle, String assignee, String priority) {
        String channel = priority.equals("CRITICAL") ? "SMS" : "EMAIL";
        String message = String.format("📋 Nova tarefa: '%s' (Prioridade: %s) para %s",
                taskTitle, priority, assignee);

        log.info("📧 [NOTIFICAÇÃO] Canal: {} | Mensagem: {}", channel, message);

        Notification notif = new Notification();
        notif.setTaskId(taskId);
        notif.setTaskTitle(taskTitle);
        notif.setRecipient(assignee);
        notif.setChannel(channel);
        notif.setMessage(message);
        notif.setSentAt(LocalDateTime.now());
        notif.setSuccess(true);
        notificationRepository.save(notif);

        log.info("✅ Notificação registrada para tarefa {}", taskId);
    }
}
