package com.example.kafka.processor.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/file-processor")
@RequiredArgsConstructor
public class FileProcessorController {

    private final JobLauncher jobLauncher;
    private final Job processFileJob;

    @PostMapping("/process")
    public ResponseEntity<String> processFile() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(processFileJob, jobParameters);

            return ResponseEntity.ok("File processing job started successfully");
        } catch (Exception e) {
            log.error("Error starting file processing job", e);
            return ResponseEntity.internalServerError()
                    .body("Error: " + e.getMessage());
        }
    }
}

