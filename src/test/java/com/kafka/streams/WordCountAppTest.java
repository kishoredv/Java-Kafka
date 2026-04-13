package com.kafka.streams;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link WordCountApp}.
 *
 * <p>Uses {@link TopologyTestDriver} – no real Kafka broker required.
 */
class WordCountAppTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, String> outputTopic;

    @BeforeEach
    void setUp() {
        Properties props = WordCountApp.buildProperties("dummy:9092");
        testDriver = new TopologyTestDriver(WordCountApp.buildTopology(), props);

        inputTopic = testDriver.createInputTopic(
                WordCountApp.INPUT_TOPIC,
                new StringSerializer(), new StringSerializer());

        outputTopic = testDriver.createOutputTopic(
                WordCountApp.OUTPUT_TOPIC,
                new StringDeserializer(), new StringDeserializer());
    }

    @AfterEach
    void tearDown() {
        testDriver.close();
    }

    @Test
    void singleWord_shouldHaveCountOne() {
        inputTopic.pipeInput("msg-1", "hello");

        Map<String, String> result = outputTopic.readKeyValuesToMap();
        assertEquals("1", result.get("hello"), "Expected count 1 for 'hello'");
    }

    @Test
    void repeatedWord_shouldAccumulateCount() {
        inputTopic.pipeInput("msg-1", "kafka");
        inputTopic.pipeInput("msg-2", "kafka");
        inputTopic.pipeInput("msg-3", "kafka");

        Map<String, String> result = outputTopic.readKeyValuesToMap();
        assertEquals("3", result.get("kafka"), "Expected count 3 for 'kafka'");
    }

    @Test
    void sentenceInput_shouldCountEachWordIndependently() {
        inputTopic.pipeInput("msg-1", "hello world hello kafka");

        Map<String, String> result = outputTopic.readKeyValuesToMap();
        assertEquals("2", result.get("hello"), "Expected count 2 for 'hello'");
        assertEquals("1", result.get("world"),  "Expected count 1 for 'world'");
        assertEquals("1", result.get("kafka"),  "Expected count 1 for 'kafka'");
    }

    @Test
    void mixedCase_shouldBeTreatedAsSameWord() {
        inputTopic.pipeInput("msg-1", "Kafka KAFKA kafka");

        Map<String, String> result = outputTopic.readKeyValuesToMap();
        assertEquals("3", result.get("kafka"), "Case-insensitive count should be 3");
    }

    @Test
    void emptyInput_shouldProduceNoOutput() {
        // no records piped
        assertTrue(outputTopic.isEmpty(), "Output should be empty when no input is given");
    }

    @Test
    void buildProperties_shouldContainRequiredKeys() {
        Properties props = WordCountApp.buildProperties("localhost:9092");
        assertEquals(WordCountApp.APP_ID, props.getProperty("application.id"));
        assertEquals("localhost:9092", props.getProperty("bootstrap.servers"));
    }
}
