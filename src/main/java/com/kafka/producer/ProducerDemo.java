package com.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * ProducerDemo – illustrates the Kafka producer API.
 *
 * <p>Concepts covered:
 * <ul>
 *   <li>Creating a {@link KafkaProducer} with minimal configuration</li>
 *   <li>Sending messages <em>asynchronously</em> (fire-and-forget)</li>
 *   <li>Sending messages <em>synchronously</em> (blocking .get())</li>
 *   <li>Attaching a callback to inspect {@link RecordMetadata}</li>
 *   <li>Properly closing the producer to flush outstanding messages</li>
 * </ul>
 *
 * <p>Prerequisites: a running Kafka broker at {@code localhost:9092} and a topic
 * named {@value #TOPIC}.  Start one quickly with Docker:
 * <pre>
 *   docker run -d -p 9092:9092 apache/kafka:3.9.2
 * </pre>
 */
public class ProducerDemo {

    private static final Logger log = LoggerFactory.getLogger(ProducerDemo.class);

    public static final String TOPIC = "demo-topic";

    // -----------------------------------------------------------------------
    // Factory helper – separated so tests can inject a mock producer
    // -----------------------------------------------------------------------

    /**
     * Builds the default {@link Properties} used to connect to a local broker.
     */
    public static Properties buildProperties(String bootstrapServers) {
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        // Idempotent producer = exactly-once delivery within a session
        props.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        return props;
    }

    // -----------------------------------------------------------------------
    // Core logic – reusable from tests with an injected producer
    // -----------------------------------------------------------------------

    /**
     * Sends {@code count} numbered messages to {@code topic} using
     * {@code producer}, then flushes.
     *
     * @return the {@link RecordMetadata} of the <em>last</em> sent record
     */
    public RecordMetadata sendMessages(Producer<String, String> producer,
                                       String topic,
                                       int count) throws ExecutionException, InterruptedException {
        RecordMetadata last = null;
        for (int i = 0; i < count; i++) {
            String key = "key-" + i;
            String value = "Hello Kafka " + i;
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);

            Future<RecordMetadata> future = producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    log.error("Error sending record", exception);
                } else {
                    log.info("Sent record → topic={} partition={} offset={}",
                            metadata.topic(), metadata.partition(), metadata.offset());
                }
            });

            last = future.get(); // block until acknowledged (synchronous style)
        }
        producer.flush();
        return last;
    }

    // -----------------------------------------------------------------------
    // main – run against a real broker
    // -----------------------------------------------------------------------

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Properties props = buildProperties("localhost:9092");

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            ProducerDemo demo = new ProducerDemo();
            demo.sendMessages(producer, TOPIC, 10);
            log.info("All messages sent successfully.");
        }
    }
}
