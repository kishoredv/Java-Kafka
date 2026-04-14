package com.example.kafka.publisher.service;

import com.example.kafka.model.OutboxEvent;
import com.example.kafka.publisher.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPublisherService {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topic.transaction-events}")
    private String transactionTopic;

    @Scheduled(fixedDelayString = "${outbox.polling.interval:5000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findUnprocessedEvents();

        log.info("Found {} pending outbox events to publish", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                publishEvent(event);
            } catch (Exception e) {
                log.error("Error publishing event: {}", event.getId(), e);
                handlePublishError(event, e);
            }
        }
    }

    private void publishEvent(OutboxEvent event) {
        log.info("Publishing event {} to Kafka topic: {}", event.getId(), transactionTopic);

        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                transactionTopic,
                event.getAggregateId().toString(),
                event.getPayload()
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                markEventAsProcessed(event);
                log.info("Successfully published event {} to partition {} with offset {}",
                        event.getId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish event {}", event.getId(), ex);
                handlePublishError(event, ex);
            }
        });
    }

    @Transactional
    protected void markEventAsProcessed(OutboxEvent event) {
        event.setProcessedAt(LocalDateTime.now());
        outboxEventRepository.save(event);
    }

    @Transactional
    protected void handlePublishError(OutboxEvent event, Throwable error) {
        event.setRetryCount(event.getRetryCount() + 1);
        event.setErrorMessage(error.getMessage());

        if (event.getRetryCount() >= 3) {
            log.error("Event {} exceeded max retry count, marking as failed", event.getId());
        }

        outboxEventRepository.save(event);
    }
}

