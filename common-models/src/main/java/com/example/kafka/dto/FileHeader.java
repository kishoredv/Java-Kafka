package com.example.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileHeader {
    private String recordType;
    private String batchId;
    private String creationDate;
    private String sourceSystem;
}

