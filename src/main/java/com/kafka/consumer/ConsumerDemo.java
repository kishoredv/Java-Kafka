package com.kafka.consumer;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * ConsumerDemo – illustrates the Kafka consumer API.
 *
 * <p>Concepts covered:
 * <ul>
 *   <li>Creating a {@link KafkaConsumer} with a consumer group</li>
 *   <li>Subscribing to one or more topics</li>
 *   <li>The poll loop and how to process {@link ConsumerRecords}</li>
 *   <li>Committing offsets (auto-commit is enabled for simplicity)</li>
 *   <li>Graceful shutdown using a shutdown hook</li>
 * </ul>
 *
 * <p>Prerequisites: a running Kafka broker at {@code localhost:9092} and messages
 * already produced to the topic (run {@link com.kafka.producer.ProducerDemo} first).
 */
public class ConsumerDemo {

    private static final Logger log = LoggerFactory.getLogger(ConsumerDemo.class);

    public static final String TOPIC = "demo-topic";
    public static final String GROUP_ID = "demo-consumer-group";

    // -----------------------------------------------------------------------
    // Factory helper
    // -----------------------------------------------------------------------

    /**
     * Builds the default {@link Properties} used to connect to a local broker.
     *
     * <p>{@code auto.offset.reset=earliest} ensures we read from the very
     * beginning of the topic when no committed offset exists for the group.
     */
    public static Properties buildProperties(String bootstrapServers) {
        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        return props;
    }

    // -----------------------------------------------------------------------
    // Core logic – reusable from tests with an injected consumer
    // -----------------------------------------------------------------------

    /**
     * Polls {@code consumer} up to {@code maxPolls} times and collects all
     * received records.
     *
     * @param consumer   an already-subscribed {@link Consumer}
     * @param maxPolls   maximum number of {@link Consumer#poll} calls before returning
     * @param pollTimeout timeout passed to each {@code poll()} call
     * @return all records received across all polls
     */
    public List<ConsumerRecord<String, String>> pollRecords(
            Consumer<String, String> consumer,
            int maxPolls,
            Duration pollTimeout) {

        List<ConsumerRecord<String, String>> collected = new ArrayList<>();

        for (int i = 0; i < maxPolls; i++) {
            ConsumerRecords<String, String> records = consumer.poll(pollTimeout);
            for (ConsumerRecord<String, String> record : records) {
                log.info("Received → topic={} partition={} offset={} key={} value={}",
                        record.topic(), record.partition(), record.offset(),
                        record.key(), record.value());
                collected.add(record);
            }
            if (!records.isEmpty()) {
                // Stop early once we have received some messages
                break;
            }
        }

        return collected;
    }

    // -----------------------------------------------------------------------
    // main – run against a real broker
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        Properties props = buildProperties("localhost:9092");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(TOPIC));

            // Graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutdown detected – waking consumer...");
                consumer.wakeup();
            }));

            log.info("Polling for messages on topic '{}' (Ctrl-C to stop)…", TOPIC);
            ConsumerDemo demo = new ConsumerDemo();
            // Poll continuously until interrupted
            while (true) {
                demo.pollRecords(consumer, 1, Duration.ofMillis(1000));
            }
        } catch (org.apache.kafka.common.errors.WakeupException e) {
            log.info("Consumer woken up – shutting down.");
        }
    }
}
