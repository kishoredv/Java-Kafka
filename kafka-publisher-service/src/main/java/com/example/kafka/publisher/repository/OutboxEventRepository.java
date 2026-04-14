package com.example.kafka.publisher.repository;

import com.example.kafka.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("SELECT o FROM OutboxEvent o WHERE o.processedAt IS NULL AND o.retryCount < 3 ORDER BY o.createdAt ASC")
    List<OutboxEvent> findUnprocessedEvents();
}

