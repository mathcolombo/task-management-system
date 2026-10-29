package com.example.task.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TaskCreatedEvent extends TaskEvent {
    private String title;
    private String description;
    private String assignee;
    private String priority;

    public TaskCreatedEvent(Long taskId, String title, String description,
                            String assignee, String priority) {
        super("TASK_CREATED", taskId);
        this.title = title;
        this.description = description;
        this.assignee = assignee;
        this.priority = priority;
    }
}
