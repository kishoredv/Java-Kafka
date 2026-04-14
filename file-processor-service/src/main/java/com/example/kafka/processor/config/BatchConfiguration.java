package com.example.kafka.processor.config;

import com.example.kafka.model.Transaction;
import com.example.kafka.processor.batch.TransactionFileReader;
import com.example.kafka.processor.batch.TransactionItemProcessor;
import com.example.kafka.processor.batch.TransactionItemWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BatchConfiguration {

    @Value("${file.input.path:input/transactions.txt}")
    private String inputFilePath;

    private final TransactionItemProcessor processor;
    private final TransactionItemWriter writer;

    @Bean
    public TransactionFileReader reader() {
        return new TransactionFileReader(inputFilePath);
    }

    @Bean
    public Step processFileStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager) {
        return new StepBuilder("processFileStep", jobRepository)
                .<Transaction, Transaction>chunk(10, transactionManager)
                .reader(reader())
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job processFileJob(JobRepository jobRepository,
                              Step processFileStep) {
        return new JobBuilder("processFileJob", jobRepository)
                .start(processFileStep)
                .build();
    }
}

