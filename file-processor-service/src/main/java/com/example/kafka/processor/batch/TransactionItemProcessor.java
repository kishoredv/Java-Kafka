package com.example.kafka.processor.batch;

import com.example.kafka.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionItemProcessor implements ItemProcessor<Transaction, Transaction> {

    @Override
    public Transaction process(Transaction transaction) throws Exception {
        // Validate transaction
        if (transaction.getCardNumber() == null || transaction.getCardNumber().length() < 13) {
            log.warn("Invalid card number: {}", transaction.getCardNumber());
            transaction.setStatus("INVALID");
            return transaction;
        }

        if (transaction.getAmount() == null || transaction.getAmount().signum() <= 0) {
            log.warn("Invalid amount: {}", transaction.getAmount());
            transaction.setStatus("INVALID");
            return transaction;
        }

        // Process transaction
        log.debug("Processing transaction: {}", transaction);
        transaction.setStatus("PROCESSED");

        return transaction;
    }
}

