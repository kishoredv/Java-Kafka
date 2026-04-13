package com.kafka.producer;

import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ProducerDemo}.
 *
 * <p>Uses {@link MockProducer} so no real Kafka broker is required.
 */
class ProducerDemoTest {

    private MockProducer<String, String> mockProducer;
    private ProducerDemo demo;

    @BeforeEach
    void setUp() {
        // autoComplete=true means records are immediately "acknowledged"
        mockProducer = new MockProducer<>(true, new StringSerializer(), new StringSerializer());
        demo = new ProducerDemo();
    }

    @AfterEach
    void tearDown() {
        mockProducer.close();
    }

    @Test
    void sendMessages_shouldSendCorrectNumberOfMessages() throws ExecutionException, InterruptedException {
        demo.sendMessages(mockProducer, ProducerDemo.TOPIC, 5);

        List<ProducerRecord<String, String>> history = mockProducer.history();
        assertEquals(5, history.size(), "Expected exactly 5 records in producer history");
    }

    @Test
    void sendMessages_recordsShouldHaveExpectedKeysAndValues() throws ExecutionException, InterruptedException {
        demo.sendMessages(mockProducer, ProducerDemo.TOPIC, 3);

        List<ProducerRecord<String, String>> history = mockProducer.history();
        for (int i = 0; i < 3; i++) {
            ProducerRecord<String, String> record = history.get(i);
            assertEquals(ProducerDemo.TOPIC, record.topic());
            assertEquals("key-" + i, record.key());
            assertEquals("Hello Kafka " + i, record.value());
        }
    }

    @Test
    void sendMessages_shouldReturnNonNullMetadata() throws ExecutionException, InterruptedException {
        var metadata = demo.sendMessages(mockProducer, ProducerDemo.TOPIC, 1);
        assertNotNull(metadata, "RecordMetadata should not be null");
    }

    @Test
    void buildProperties_shouldContainRequiredKeys() {
        var props = ProducerDemo.buildProperties("localhost:9092");
        assertEquals("localhost:9092", props.getProperty("bootstrap.servers"));
        assertNotNull(props.getProperty("key.serializer"));
        assertNotNull(props.getProperty("value.serializer"));
    }
}
