package com.example.analytics.service;

import com.example.analytics.model.TaskMetric;
import com.example.analytics.repository.TaskMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final TaskMetricRepository metricRepository;

    @Transactional
    public void recordTaskCreated(String priority) {
        LocalDate today = LocalDate.now();
        TaskMetric metric = metricRepository.findByDate(today)
                .orElse(new TaskMetric());

        metric.setDate(today);
        metric.setTotalTasks(metric.getTotalTasks() != null ? metric.getTotalTasks() + 1 : 1);

        switch (priority) {
            case "CRITICAL" -> metric.setCriticalPriorityTasks(
                    metric.getCriticalPriorityTasks() != null ? metric.getCriticalPriorityTasks() + 1 : 1);
            case "HIGH" -> metric.setHighPriorityTasks(
                    metric.getHighPriorityTasks() != null ? metric.getHighPriorityTasks() + 1 : 1);
            case "MEDIUM" -> metric.setMediumPriorityTasks(
                    metric.getMediumPriorityTasks() != null ? metric.getMediumPriorityTasks() + 1 : 1);
            case "LOW" -> metric.setLowPriorityTasks(
                    metric.getLowPriorityTasks() != null ? metric.getLowPriorityTasks() + 1 : 1);
        }

        metricRepository.save(metric);
        log.info("📊 Métrica atualizada: {} | Total: {} | Críticas: {}",
                today, metric.getTotalTasks(), metric.getCriticalPriorityTasks());
    }
}
