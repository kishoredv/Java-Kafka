package com.kafka.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;

/**
 * WordCountApp – illustrates the Kafka Streams DSL.
 *
 * <p>Concepts covered:
 * <ul>
 *   <li>Building a {@link Topology} with the high-level Streams DSL</li>
 *   <li>Stateless transformations: {@code flatMapValues}, {@code mapValues}</li>
 *   <li>Stateful aggregations: {@code groupByKey} + {@code count} → {@link KTable}</li>
 *   <li>Writing results back to an output topic</li>
 *   <li>Graceful shutdown of a {@link KafkaStreams} application</li>
 * </ul>
 *
 * <p>Data flow:
 * <pre>
 *   input-topic  →  split words  →  groupByKey  →  count  →  word-count-output
 * </pre>
 *
 * <p>Prerequisites: a running Kafka broker at {@code localhost:9092} with topics
 * {@value #INPUT_TOPIC} and {@value #OUTPUT_TOPIC}.
 */
public class WordCountApp {

    private static final Logger log = LoggerFactory.getLogger(WordCountApp.class);

    public static final String INPUT_TOPIC = "word-count-input";
    public static final String OUTPUT_TOPIC = "word-count-output";
    public static final String APP_ID = "word-count-app";

    // -----------------------------------------------------------------------
    // Topology – separated from configuration so it can be unit-tested
    // -----------------------------------------------------------------------

    /**
     * Builds and returns the word-count {@link Topology}.
     *
     * <p>The topology:
     * <ol>
     *   <li>Reads strings from {@value #INPUT_TOPIC}</li>
     *   <li>Lower-cases and splits each value into words</li>
     *   <li>Re-keys each word as the record key</li>
     *   <li>Groups by key and counts occurrences in a {@link KTable}</li>
     *   <li>Writes (word, count) pairs to {@value #OUTPUT_TOPIC}</li>
     * </ol>
     */
    public static Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        // 1. Source stream
        KStream<String, String> textLines = builder.stream(INPUT_TOPIC);

        // 2. Word count
        KTable<String, Long> wordCounts = textLines
                .flatMapValues(value -> Arrays.asList(value.toLowerCase().split("\\W+")))
                .filter((key, word) -> !word.isBlank())
                .groupBy((key, word) -> word)
                .count(Materialized.as("word-count-store"));

        // 3. Sink: write (word, count-as-string) to the output topic
        wordCounts.toStream()
                .mapValues(count -> Long.toString(count))
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));

        return builder.build();
    }

    // -----------------------------------------------------------------------
    // Properties factory
    // -----------------------------------------------------------------------

    public static Properties buildProperties(String bootstrapServers) {
        Properties props = new Properties();
        props.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, APP_ID);
        props.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
                Serdes.String().getClass().getName());
        props.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                Serdes.String().getClass().getName());
        // Commit every 1 second (lower latency for demo purposes)
        props.setProperty(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, "1000");
        return props;
    }

    // -----------------------------------------------------------------------
    // main – run against a real broker
    // -----------------------------------------------------------------------

    public static void main(String[] args) throws InterruptedException {
        Properties props = buildProperties("localhost:9092");
        Topology topology = buildTopology();

        log.info("Topology description:\n{}", topology.describe());

        KafkaStreams streams = new KafkaStreams(topology, props);

        // Graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown hook triggered – closing streams application…");
            streams.close();
        }));

        streams.start();
        log.info("Word-count Streams application started.  Ctrl-C to stop.");
    }
}
