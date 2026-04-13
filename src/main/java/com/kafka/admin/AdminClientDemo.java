package com.kafka.admin;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.errors.TopicExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * AdminClientDemo – illustrates the Kafka AdminClient API.
 *
 * <p>Concepts covered:
 * <ul>
 *   <li>Creating topics programmatically</li>
 *   <li>Listing existing topics</li>
 *   <li>Describing a topic (partitions, replicas, ISR)</li>
 *   <li>Deleting a topic</li>
 * </ul>
 *
 * <p>The AdminClient communicates with the Kafka broker over the same
 * bootstrap connection used by producers and consumers.
 */
public class AdminClientDemo {

    private static final Logger log = LoggerFactory.getLogger(AdminClientDemo.class);

    // -----------------------------------------------------------------------
    // Factory helper
    // -----------------------------------------------------------------------

    public static Properties buildProperties(String bootstrapServers) {
        Properties props = new Properties();
        props.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return props;
    }

    // -----------------------------------------------------------------------
    // Core operations – each method accepts an AdminClient so they are testable
    // -----------------------------------------------------------------------

    /**
     * Creates a topic if it does not already exist.
     *
     * @param adminClient connected {@link AdminClient}
     * @param topicName   name of the topic
     * @param partitions  number of partitions
     * @param replication replication factor
     */
    public void createTopic(AdminClient adminClient,
                            String topicName,
                            int partitions,
                            short replication)
            throws ExecutionException, InterruptedException {

        NewTopic newTopic = new NewTopic(topicName, partitions, replication);
        try {
            adminClient.createTopics(Collections.singleton(newTopic)).all().get();
            log.info("Topic '{}' created ({} partitions, replication factor {}).",
                    topicName, partitions, replication);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof TopicExistsException) {
                log.warn("Topic '{}' already exists – skipping creation.", topicName);
            } else {
                throw e;
            }
        }
    }

    /**
     * Returns the names of all topics visible to this client.
     */
    public Set<String> listTopics(AdminClient adminClient)
            throws ExecutionException, InterruptedException {
        Set<String> topics = adminClient.listTopics().names().get();
        log.info("Topics: {}", topics);
        return topics;
    }

    /**
     * Describes a topic and logs partition metadata.
     */
    public TopicDescription describeTopic(AdminClient adminClient, String topicName)
            throws ExecutionException, InterruptedException {
        Map<String, TopicDescription> descriptions =
                adminClient.describeTopics(Collections.singleton(topicName)).allTopicNames().get();
        TopicDescription desc = descriptions.get(topicName);
        log.info("Topic description: {}", desc);
        return desc;
    }

    /**
     * Deletes a topic.
     */
    public void deleteTopic(AdminClient adminClient, String topicName)
            throws ExecutionException, InterruptedException {
        adminClient.deleteTopics(Collections.singleton(topicName)).all().get();
        log.info("Topic '{}' deleted.", topicName);
    }

    // -----------------------------------------------------------------------
    // main – run against a real broker
    // -----------------------------------------------------------------------

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Properties props = buildProperties("localhost:9092");

        try (AdminClient adminClient = AdminClient.create(props)) {
            AdminClientDemo demo = new AdminClientDemo();
            demo.createTopic(adminClient, "my-new-topic", 3, (short) 1);
            demo.listTopics(adminClient);
            demo.describeTopic(adminClient, "my-new-topic");
            demo.deleteTopic(adminClient, "my-new-topic");
        }
    }
}
