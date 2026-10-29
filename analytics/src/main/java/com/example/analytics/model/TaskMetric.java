package com.example.analytics.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskMetric {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    private Long totalTasks;
    private Long highPriorityTasks;
    private Long mediumPriorityTasks;
    private Long lowPriorityTasks;
    private Long criticalPriorityTasks;
}
