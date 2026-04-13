package com.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ConsumerDemo}.
 *
 * <p>Uses {@link MockConsumer} so no real Kafka broker is required.
 */
class ConsumerDemoTest {

    private MockConsumer<String, String> mockConsumer;
    private ConsumerDemo demo;

    private static final TopicPartition PARTITION =
            new TopicPartition(ConsumerDemo.TOPIC, 0);

    @BeforeEach
    void setUp() {
        mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        demo = new ConsumerDemo();

        // Subscribe and set up initial offsets
        mockConsumer.subscribe(Collections.singletonList(ConsumerDemo.TOPIC));
        mockConsumer.rebalance(Collections.singletonList(PARTITION));

        Map<TopicPartition, Long> beginningOffsets = new HashMap<>();
        beginningOffsets.put(PARTITION, 0L);
        mockConsumer.updateBeginningOffsets(beginningOffsets);
    }

    @AfterEach
    void tearDown() {
        mockConsumer.close();
    }

    @Test
    void pollRecords_shouldReturnEmptyListWhenNoMessages() {
        List<ConsumerRecord<String, String>> records =
                demo.pollRecords(mockConsumer, 1, Duration.ofMillis(100));
        assertTrue(records.isEmpty(), "Expected no records when topic is empty");
    }

    @Test
    void pollRecords_shouldReturnAllStagedRecords() {
        // Stage two records in the mock consumer
        mockConsumer.addRecord(new ConsumerRecord<>(ConsumerDemo.TOPIC, 0, 0L, "k1", "value-1"));
        mockConsumer.addRecord(new ConsumerRecord<>(ConsumerDemo.TOPIC, 0, 1L, "k2", "value-2"));

        List<ConsumerRecord<String, String>> records =
                demo.pollRecords(mockConsumer, 3, Duration.ofMillis(100));

        assertEquals(2, records.size());
        assertEquals("value-1", records.get(0).value());
        assertEquals("value-2", records.get(1).value());
    }

    @Test
    void pollRecords_stopsAfterFirstNonEmptyBatch() {
        // Stage records only for first poll; the consumer should stop after receiving them
        mockConsumer.addRecord(new ConsumerRecord<>(ConsumerDemo.TOPIC, 0, 0L, "k1", "hello"));

        List<ConsumerRecord<String, String>> records =
                demo.pollRecords(mockConsumer, 5, Duration.ofMillis(100));

        assertEquals(1, records.size());
    }

    @Test
    void buildProperties_shouldContainRequiredKeys() {
        var props = ConsumerDemo.buildProperties("localhost:9092");
        assertEquals("localhost:9092", props.getProperty("bootstrap.servers"));
        assertEquals(ConsumerDemo.GROUP_ID, props.getProperty("group.id"));
        assertEquals("earliest", props.getProperty("auto.offset.reset"));
    }
}
