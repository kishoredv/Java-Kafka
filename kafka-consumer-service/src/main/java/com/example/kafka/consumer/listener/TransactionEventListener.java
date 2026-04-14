package com.example.kafka.consumer.listener;

import com.example.kafka.dto.TransactionEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionEventListener {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @KafkaListener(
            topics = "${kafka.topic.transaction-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(@Payload String message,
                      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                      @Header(KafkaHeaders.OFFSET) long offset,
                      @Header(KafkaHeaders.RECEIVED_KEY) String key,
                      Acknowledgment acknowledgment) {

        log.info("Received message from partition {} with offset {}", partition, offset);
        log.debug("Message key: {}, Message: {}", key, message);

        try {
            // Parse the JSON message to TransactionEvent
            TransactionEvent event = objectMapper.readValue(message, TransactionEvent.class);

            // Process the transaction event
            processTransactionEvent(event);

            // Acknowledge the message after successful processing
            acknowledgment.acknowledge();
            log.info("Successfully processed and acknowledged transaction event: {}",
                    event.getTransactionId());

        } catch (Exception e) {
            log.error("Error processing message from partition {} with offset {}",
                    partition, offset, e);
            // In production, implement retry logic or dead letter queue
            // For now, we'll acknowledge to avoid blocking the consumer
            acknowledgment.acknowledge();
        }
    }

    private void processTransactionEvent(TransactionEvent event) {
        log.info("Processing Transaction Event:");
        log.info("  Transaction ID: {}", event.getTransactionId());
        log.info("  Transaction Type: {}", event.getTransactionType());
        log.info("  Card Number: {}", event.getCardNumber());
        log.info("  Account Number: {}", event.getAccountNumber());
        log.info("  Amount: {}", event.getAmount());
        log.info("  Batch ID: {}", event.getBatchId());
        log.info("  Status: {}", event.getStatus());
        log.info("  Timestamp: {}", event.getTimestamp());

        // Implement your business logic here
        // Examples:
        // - Update account balance
        // - Send notifications
        // - Trigger fraud detection
        // - Update analytics

        // Simulate some processing
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("Transaction processing completed for ID: {}", event.getTransactionId());
    }
}

