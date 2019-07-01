package playproject.springkafkawordcount.wordcount;

import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.aws.autoconfigure.context.ContextResourceLoaderAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.context.ContextStackAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.messaging.MessagingAutoConfiguration;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import playproject.springkafkawordcount.AbstractKafkaTest;
import playproject.springkafkawordcount.KafkaNullConfiguration;
import playproject.springkafkawordcount.infrastructure.model.event.TextEvent;
import playproject.springkafkawordcount.wordcount.spring.WordCountProcessorBinding;
import playproject.springkafkawordcount.wordcount.spring.WordCountProcessorConfiguration;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static playproject.springkafkawordcount.wordcount.WordCountProcessor.WORD_COUNT_STORE;

@EnableAutoConfiguration(exclude = {MessagingAutoConfiguration.class
        , ContextStackAutoConfiguration.class
        , ContextResourceLoaderAutoConfiguration.class
})
@EmbeddedKafka(partitions = 1
        ,topics = {"textInput", "wordCount", "windowedWordCount"})
@SpringBootTest(classes = WordCountProcessorTest.TestConfiguration.class
        , properties = {
        "word-count-processor.window-duration-secs=2"
        ,"spring.cloud.stream.bindings.test-text-input.destination=textInput"
        ,"spring.cloud.stream.kafka.streams.bindings.test-text-input.consumer.application-id=WordCountProcessorTest"
        ,"spring.cloud.stream.bindings.test-text-input.binder=kafka"
        ,"spring.cloud.stream.kafka.streams.binder.configuration.default.key.serde=org.apache.kafka.common.serialization.Serdes$StringSerde"
        ,"spring.cloud.stream.kafka.streams.binder.configuration.default.value.serde=org.apache.kafka.common.serialization.Serdes$StringSerde"
        ,"spring.cloud.zookeeper.discovery.instance-port=80"
        })
class WordCountProcessorTest extends AbstractKafkaTest {

    @Autowired
    @Qualifier("test-text-input")
    private
    MessageChannel textInput;

    @Autowired
    private InteractiveQueryService interactiveQueryService;

    @Test
    void process() {

        var wordCountStore = interactiveQueryService.getQueryableStore(WORD_COUNT_STORE, QueryableStoreTypes.<String, Long>keyValueStore());

        Long oneCount = getWordCount(wordCountStore, "one");
        Long twoCount = getWordCount(wordCountStore, "two");
        Long threeCount = getWordCount(wordCountStore, "three");

        textInput.send(new GenericMessage<>(new TextEvent("one")));
        sleep(Duration.ofSeconds(30));

        verifyEvents("wordCount", events -> assertThat(events, contains(
                "{\"word\":\"one\",\"count\":" + (oneCount + 1) + "}"
        )));

        verifyEvents("windowedWordCount", events -> assertThat(events, contains(
                "{\"word\":\"one\",\"count\":1}"
        )));

        textInput.send(new GenericMessage<>(new TextEvent("one two")));
        textInput.send(new GenericMessage<>(new TextEvent("one two three")));
        sleep(Duration.ofSeconds(30));

        verifyEvents("wordCount", events -> assertThat(events, contains(
                "{\"word\":\"one\",\"count\":" + (oneCount + 3) + "}",
                "{\"word\":\"two\",\"count\":" + (twoCount + 2) + "}",
                "{\"word\":\"three\",\"count\":" + (threeCount + 1) + "}"
        )));

        verifyEvents("windowedWordCount", events -> assertThat(events, contains(
                "{\"word\":\"one\",\"count\":2}",
                "{\"word\":\"two\",\"count\":2}",
                "{\"word\":\"three\",\"count\":1}"
        )));
    }

    @Import({WordCountProcessorConfiguration.class, KafkaNullConfiguration.class})
    @EnableBinding({ WordCountProcessorBinding.class, TestBinding.class})
    @EntityScan(basePackageClasses = WordCountProcessor.class)
    public static class TestConfiguration extends AbstractKafkaTest.TestConfiguration {
    }

    public interface TestBinding {
        @Output("test-text-input")
        MessageChannel textInput();
    }

    private Long getWordCount(ReadOnlyKeyValueStore<String, Long> store, String word) {
        Long count = store.get(word);
        return count == null ? 0 : count;
    }
}