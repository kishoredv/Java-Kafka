package com.example.kafka.processor.batch;

import com.example.kafka.dto.TransactionEvent;
import com.example.kafka.model.OutboxEvent;
import com.example.kafka.model.Transaction;
import com.example.kafka.processor.repository.OutboxEventRepository;
import com.example.kafka.processor.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionItemWriter implements ItemWriter<Transaction> {

    private final TransactionRepository transactionRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    @Transactional
    public void write(Chunk<? extends Transaction> chunk) throws Exception {
        for (Transaction transaction : chunk) {
            // Save transaction to database
            Transaction savedTransaction = transactionRepository.save(transaction);
            log.info("Saved transaction: {}", savedTransaction.getId());

            // Create outbox event for Kafka publishing
            TransactionEvent event = TransactionEvent.builder()
                    .transactionId(savedTransaction.getId())
                    .transactionType(savedTransaction.getTransactionType())
                    .cardNumber(maskCardNumber(savedTransaction.getCardNumber()))
                    .accountNumber(savedTransaction.getAccountNumber())
                    .amount(savedTransaction.getAmount())
                    .batchId(savedTransaction.getBatchId())
                    .status(savedTransaction.getStatus())
                    .timestamp(LocalDateTime.now())
                    .eventType("TRANSACTION_CREATED")
                    .build();

            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateId(savedTransaction.getId())
                    .aggregateType("TRANSACTION")
                    .eventType("TRANSACTION_CREATED")
                    .payload(payload)
                    .build();

            outboxEventRepository.save(outboxEvent);
            log.info("Created outbox event: {}", outboxEvent.getId());
        }
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "************" + cardNumber.substring(cardNumber.length() - 4);
    }
}

