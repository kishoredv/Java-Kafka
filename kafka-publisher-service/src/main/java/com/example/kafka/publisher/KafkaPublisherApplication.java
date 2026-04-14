package com.example.kafka.publisher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = "com.example.kafka.model")
public class KafkaPublisherApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaPublisherApplication.class, args);
    }
}

