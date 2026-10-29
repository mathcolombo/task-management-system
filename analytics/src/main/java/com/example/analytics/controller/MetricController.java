package com.example.analytics.controller;

import com.example.analytics.model.TaskMetric;
import com.example.analytics.repository.TaskMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricController {

    private final TaskMetricRepository metricRepository;

    @GetMapping
    public ResponseEntity<List<TaskMetric>> getAllMetrics() {
        return ResponseEntity.ok(metricRepository.findAll());
    }
}
