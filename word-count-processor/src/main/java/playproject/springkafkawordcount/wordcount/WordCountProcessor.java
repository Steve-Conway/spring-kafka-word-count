package playproject.springkafkawordcount.wordcount;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.*;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;
import playproject.springkafkawordcount.infrastructure.model.WordCount;
import playproject.springkafkawordcount.infrastructure.model.event.TextEvent;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequestMapping()
public class WordCountProcessor {

    static final String WORD_COUNT_STORE = "word-count-store";
    static final String WINDOWED_WORD_COUNT_STORE = "windowed-word-count-store";

    private final int windowDurationSecs;

    private InteractiveQueryService interactiveQueryService;

    private final StoreFactory wordCountStoreFactory;
    private final WindowStoreFactory windowedWordCountStoreFactory;

    public WordCountProcessor(int windowDurationSecs, InteractiveQueryService interactiveQueryService) {
        this.windowDurationSecs = windowDurationSecs;
        this.interactiveQueryService = interactiveQueryService;
        wordCountStoreFactory = new StoreFactory(WORD_COUNT_STORE);
        windowedWordCountStoreFactory = new WindowStoreFactory(WINDOWED_WORD_COUNT_STORE);
    }

    @StreamListener("text-input")
    @SendTo("word-count")
    public KStream<?, WordCount> process(KStream<Bytes, TextEvent> input) {

        return input
                .flatMapValues(value -> Arrays.asList(value.getPayload().toLowerCase().split("\\W+")))
                .peek((key, word) -> log.info("Counting word: \"" + word + "\""))
                .groupBy((key, word) -> word)
                .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>
                        as(WORD_COUNT_STORE).withKeySerde(Serdes.String()).withValueSerde(Serdes.Long()))
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
                .count(Materialized.<String, Long, WindowStore<Bytes, byte[]>>
                        as(WINDOWED_WORD_COUNT_STORE).withKeySerde(Serdes.String()).withValueSerde(Serdes.Long()))
                .toStream()
                .map((key, value) -> new KeyValue<>(null, new WordCount(key.key(), value)))
                .peek((key, wordCount) -> log.info("Output (windowed): " + wordCount))
                ;
    }

    @GetMapping("/wordcount")
    @ResponseBody
    public List<WordCount> wordCount() {
        ReadOnlyKeyValueStore<String, Long> store = wordCountStoreFactory.getStore();
        try (KeyValueIterator<String, Long> storeIterator = store.all()) {
            List<WordCount> wordCounts = new ArrayList<>((int)store.approximateNumEntries());
            storeIterator.forEachRemaining(kv -> wordCounts.add(new WordCount(kv.key, kv.value)));
            return wordCounts;
        }
    }

    @GetMapping("/wordcount/{word}")
    @ResponseBody
    public WordCount wordCount(@PathVariable String word) {
        ReadOnlyKeyValueStore<String, Long> store = wordCountStoreFactory.getStore();
        word = word.toLowerCase();
        Long count = store.get(word);
        if (count == null) {
            count = 0L;
        }
        return new WordCount(word, count);
    }

    @GetMapping("/windowedwordcount")
    @ResponseBody
    public List<WordCount> windowedWordCount() {
        ReadOnlyWindowStore<String, Long> store = windowedWordCountStoreFactory.getStore();
        Instant to = Instant.now();
        Instant from = to.minus(Duration.ofSeconds(windowDurationSecs));
        try (KeyValueIterator<Windowed<String>, Long> storeIterator = store.fetchAll(from, to)) {
            List<WordCount> wordCounts = new ArrayList<>();
            storeIterator.forEachRemaining(kv -> wordCounts.add(new WordCount(kv.key.key(), kv.value)));
            return wordCounts;
        }
    }

    @GetMapping("/windowedwordcount/{word}")
    @ResponseBody
    public WordCount windowedWordCount(@PathVariable String word) {
        ReadOnlyWindowStore<String, Long> store = windowedWordCountStoreFactory.getStore();
        word = word.toLowerCase();
        Long count = store.fetch(word, windowDurationSecs);
        if (count == null) {
            count = 0L;
        }
        return new WordCount(word, count);
    }

    @ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR,reason="No counts stored yet")
    class NoStoreException extends RuntimeException
    {
        private static final long serialVersionUID = 100L;

        private NoStoreException(String storeName) {
            super("Store " + storeName + " has not been created yet");
        }
    }

    private class StoreFactory {

        @NotNull
        private String storeName;

        StoreFactory(String storeName) throws NoStoreException {
            this.storeName = storeName;
        }

        @Getter(lazy = true)
        private final ReadOnlyKeyValueStore<String, Long> store = initStore();

        private ReadOnlyKeyValueStore<String, Long> initStore() throws NoStoreException {
            ReadOnlyKeyValueStore<String, Long> store = interactiveQueryService.getQueryableStore(storeName, QueryableStoreTypes.keyValueStore());
            if (store == null) {
                throw new NoStoreException(storeName);
            }
            return store;
        }
    }

    private class WindowStoreFactory {

        @NotNull
        private String storeName;

        WindowStoreFactory(String storeName) throws NoStoreException {
            this.storeName = storeName;
        }

        @Getter(lazy = true)
        private final ReadOnlyWindowStore<String, Long> store = initStore();

        private ReadOnlyWindowStore<String, Long> initStore() throws NoStoreException {
            ReadOnlyWindowStore<String, Long> store = interactiveQueryService.getQueryableStore(storeName, QueryableStoreTypes.windowStore());
            if (store == null) {
                throw new NoStoreException(storeName);
            }
            return store;
        }
    }

}
