package com.example.task.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class TaskEvent {
    private String eventId;
    private String eventType;
    private Long taskId;
    private LocalDateTime timestamp;

    public TaskEvent(String eventType, Long taskId) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.taskId = taskId;
        this.timestamp = LocalDateTime.now();
    }
}
