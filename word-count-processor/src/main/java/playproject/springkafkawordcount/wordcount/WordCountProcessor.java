package playproject.springkafkawordcount.wordcount;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.SendTo;
import playproject.springkafkawordcount.infrastructure.model.WordCount;
import playproject.springkafkawordcount.infrastructure.model.event.TextEvent;

import java.time.Duration;
import java.util.Arrays;

@Slf4j
public class WordCountProcessor {

    private final int windowDurationSecs;

    public WordCountProcessor(int windowDurationSecs) {
        this.windowDurationSecs = windowDurationSecs;
    }

    static final String WORD_COUNT_STORE = "word-count-store";
    static final String WINDOWED_WORD_COUNT_STORE = "windowed-word-count-store";

    @StreamListener("text-input")
    @SendTo("word-count")
    public KStream<?, WordCount> process(KStream<Bytes, TextEvent> input) {

        return input
                .flatMapValues(value -> Arrays.asList(value.getPayload().toLowerCase().split("\\W+")))
                .peek((key, word) -> log.info("Counting word: \"" + word + "\""))
                .groupBy((key, word) -> word)
                .count(Materialized.as(WORD_COUNT_STORE))
                .toStream()
                .map((key, value) -> new KeyValue<>(null, new WordCount(key, value)))
                .peek((key, wordCount) -> log.info("Output: " + wordCount))
                ;
    }

    @StreamListener("text-input-for-windowed")
    @SendTo("windowed-word-count")
    public KStream<?, WordCount> processWindowed(KStream<Bytes, TextEvent> input) {

        return input
                .flatMapValues(value -> Arrays.asList(value.getPayload().toLowerCase().split("\\W+")))
                .groupBy((key, word) -> word)
                .windowedBy(TimeWindows.of(Duration.ofSeconds(windowDurationSecs)))
                .count(Materialized.as(WINDOWED_WORD_COUNT_STORE))
                .toStream()
                .map((key, value) -> new KeyValue<>(null, new WordCount(key.key(), value)))
                .peek((key, wordCount) -> log.info("Output (windowed): " + wordCount))
                ;
    }
}
