package playproject.springkafkawordcount;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.StreamsConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.cloud.zookeeper.ZookeeperProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

@AutoConfigureDataJpa
@AutoConfigureWebMvc
@AutoConfigureMockMvc
@DirtiesContext
public abstract class AbstractKafkaTest {

    @Autowired
    protected EmbeddedKafkaBroker embeddedKafka;

    private Map<String,Long> offsets = Map.of();

    @BeforeEach
    public void setUp() {
        //populate offsets
        offsets = getCurrentOffsets();
    }

    protected void sleep(Duration wait){
        try {
            Thread.sleep(wait.toMillis());
        } catch (InterruptedException ignore) {}
    }


    protected  <K, V> List<V> getEvents(String topic
            , Class<? extends Deserializer> keyDeserializer
            , Class<? extends Deserializer> valueDeserializer
            , Duration timeout) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(UUID.randomUUID().toString(), "false", embeddedKafka);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        DefaultKafkaConsumerFactory<K,V> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        org.apache.kafka.clients.consumer.Consumer<K,V> consumer = cf.createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer,topic);
        consumer.seek(new TopicPartition(topic,0),offsets.getOrDefault(topic,0L));
        ConsumerRecords<K, V> records = KafkaTestUtils.getRecords(consumer,timeout.toMillis());
        List<V> recordList = new ArrayList<>();
        records.records(topic).forEach(cr -> recordList.add(cr.value()));
        consumer.close();
        updateOffsets(topic);
        return recordList;
    }

    protected <K,V> void verifyEvents(String topic
            , Class<? extends Deserializer> keyDeserializer
            , Class<? extends Deserializer> valueDeserializer
            , Duration timeout
            , Consumer<List<V>> test) {
        List<V> recordList = getEvents(topic, keyDeserializer, valueDeserializer, timeout);
        test.accept(recordList);
    }

    protected <K,V> void verifyEvents(String topic, Duration timeout, Consumer<List<V>> test) {
        verifyEvents(topic,IntegerDeserializer.class,StringDeserializer.class,timeout,test);
    }

    protected <V> void verifyEvents(String topic, Consumer<List<V>> test) {
        verifyEvents(topic, Duration.ofSeconds(2), test);
    }

    private Map<String, Long> getCurrentOffsets() {
        Set<String> topics = embeddedKafka.getTopics();
        Map<String,Long> offsets = new HashMap<>();
        for(String topic : topics){
            long offset = getCurrentOffset(topic);
            offsets.put(topic,offset);
        }
        return offsets;
    }

    private Map<String, Long> updateOffsets(String topic) {
        long offset = getCurrentOffset(topic);
        offsets.put(topic, offset);
        return offsets;
    }

    private <K,V> long getCurrentOffset(String topic) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(UUID.randomUUID().toString(), "false", embeddedKafka);
        DefaultKafkaConsumerFactory<K,V> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        org.apache.kafka.clients.consumer.Consumer<K,V> consumer = cf.createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer,topic);
        TopicPartition topicPartition = new TopicPartition(topic, 0);
        consumer.seekToEnd(Collections.singletonList(topicPartition));
        return consumer.position(topicPartition);
    }

    @Configuration
    public static abstract class TestConfiguration {

        @Value("${" + EmbeddedKafkaBroker.SPRING_EMBEDDED_KAFKA_BROKERS + "}")
        private String brokerAddresses;

        @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
        public KafkaStreamsConfiguration kStreamsConfigs(KafkaProperties properties) throws IOException {
            properties.setBootstrapServers(Collections.singletonList(this.brokerAddresses));
            Map<String, Object> props = new HashMap<>();
            props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
            props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, this.brokerAddresses);
            Path tmpPath = Files.createTempDirectory("kafka-streams").toAbsolutePath();
            tmpPath.toFile().deleteOnExit();
            props.put(StreamsConfig.STATE_DIR_CONFIG, tmpPath.toString());
            return new KafkaStreamsConfiguration(props);
        }

        @Bean
        public ProducerFactory<?, ?> kafkaProducerFactory(KafkaProperties properties) {
            properties.setBootstrapServers(Collections.singletonList(this.brokerAddresses));
            DefaultKafkaProducerFactory<?, ?> factory = new DefaultKafkaProducerFactory<>(
                    properties.buildProducerProperties());
            String transactionIdPrefix = properties.getProducer()
                    .getTransactionIdPrefix();
            if (transactionIdPrefix != null) {
                factory.setTransactionIdPrefix(transactionIdPrefix);
            }
            return factory;
        }

        @Bean(destroyMethod = "close")
        public CuratorFramework curatorFramework(ZookeeperProperties properties, EmbeddedKafkaBroker embeddedKafkaBroker) throws Exception {
            CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
            builder.connectString(embeddedKafkaBroker.getZookeeperConnectionString());
            CuratorFramework curator = builder.retryPolicy(new RetryOneTime(1000)).build();
            curator.start();
            curator.blockUntilConnected(properties.getBlockUntilConnectedWait(), properties.getBlockUntilConnectedUnit());
            return curator;
        }
    }
}
