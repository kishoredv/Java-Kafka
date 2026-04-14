package com.example.kafka.processor.batch;

import com.example.kafka.dto.FileHeader;
import com.example.kafka.dto.FileTrailer;
import com.example.kafka.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;

@Slf4j
public class TransactionFileReader implements ItemReader<Transaction> {

    private final String filePath;
    private BufferedReader reader;
    private FileHeader fileHeader;
    private boolean headerProcessed = false;
    private boolean finished = false;

    public TransactionFileReader(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public Transaction read() throws Exception {
        if (finished) {
            return null;
        }

        if (reader == null) {
            reader = new BufferedReader(new FileReader(filePath));
        }

        String line = reader.readLine();

        if (line == null) {
            finished = true;
            reader.close();
            return null;
        }

        // Parse record type (first character or first 3 characters)
        String recordType = line.substring(0, 3);

        if ("HDR".equals(recordType)) {
            // Header: HDR,BATCH001,2026-04-13,CORE_SYSTEM
            if (!headerProcessed) {
                fileHeader = parseHeader(line);
                headerProcessed = true;
                log.info("Processed header: {}", fileHeader);
            }
            return read(); // Skip header, read next line
        } else if ("TRL".equals(recordType)) {
            // Trailer: TRL,10,15000.00
            FileTrailer trailer = parseTrailer(line);
            log.info("Processed trailer: {}", trailer);
            finished = true;
            reader.close();
            return null; // End of file
        } else if ("TXN".equals(recordType)) {
            // Body/Transaction: TXN,PURCHASE,1234567890123456,ACC001,150.75
            return parseTransaction(line);
        }

        // Skip unknown lines
        return read();
    }

    private FileHeader parseHeader(String line) {
        String[] parts = line.split(",");
        return FileHeader.builder()
                .recordType(parts[0])
                .batchId(parts[1])
                .creationDate(parts[2])
                .sourceSystem(parts[3])
                .build();
    }

    private FileTrailer parseTrailer(String line) {
        String[] parts = line.split(",");
        return FileTrailer.builder()
                .recordType(parts[0])
                .totalRecords(Integer.parseInt(parts[1]))
                .totalAmount(parts[2])
                .build();
    }

    private Transaction parseTransaction(String line) {
        // TXN,PURCHASE,1234567890123456,ACC001,150.75
        String[] parts = line.split(",");

        return Transaction.builder()
                .transactionType(parts[1])
                .cardNumber(parts[2])
                .accountNumber(parts[3])
                .amount(new BigDecimal(parts[4]))
                .batchId(fileHeader != null ? fileHeader.getBatchId() : "UNKNOWN")
                .status("PENDING")
                .build();
    }
}

