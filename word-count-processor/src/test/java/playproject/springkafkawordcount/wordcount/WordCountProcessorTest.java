package playproject.springkafkawordcount.wordcount;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.kstream.internals.TimeWindow;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreType;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import playproject.springkafkawordcount.infrastructure.model.WordCount;
import playproject.springkafkawordcount.wordcount.WordCountProcessor.NoStoreException;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static playproject.springkafkawordcount.wordcount.WordCountProcessor.WINDOWED_WORD_COUNT_STORE;
import static playproject.springkafkawordcount.wordcount.WordCountProcessor.WORD_COUNT_STORE;

@DisplayName("Word count processor tests")
@ExtendWith(MockitoExtension.class)
class WordCountProcessorTest {

    private static final int WINDOW_DURATION_SECS = 30;
    private static final TimeWindow TIME_WINDOW = new TimeWindow(1000000L, 1030000L);

    @Mock
    private InteractiveQueryService interactiveQueryService;

    @Mock
    private ReadOnlyKeyValueStore<String, Long> readOnlyKeyValueStore;

    @Mock
    private ReadOnlyWindowStore<String, Long> readOnlyWindowStore;

    private WordCountProcessor wordCountProcessor;

    @BeforeEach
    void init() {
        wordCountProcessor = new WordCountProcessor(WINDOW_DURATION_SECS, interactiveQueryService);
    }

    @Test
    @DisplayName("Given no store then wordcount query for all words throws NoStoreException")
    void givenNoStoreThenWordCountQueryForAllWordsThrowsNoStoreException() {

        when(interactiveQueryService.getQueryableStore(eq(WORD_COUNT_STORE), any(QueryableStoreType.class))).thenReturn(null);

        assertThrows(NoStoreException.class, wordCountProcessor::wordCount);
    }

    @Test
    @DisplayName("Given empty store then wordcount query for all words returns empty list")
    void givenEmptyStoreThenWordCountQueryForAllWordsReturnsEmptyList() {

        when(interactiveQueryService.getQueryableStore(eq(WORD_COUNT_STORE), any(QueryableStoreType.class))).thenReturn(readOnlyKeyValueStore);
        when(readOnlyKeyValueStore.approximateNumEntries()).thenReturn(0L);
        when(readOnlyKeyValueStore.all()).thenReturn(new DummyKeyValueIterator<>());

        List<WordCount> wordCounts = wordCountProcessor.wordCount();

        assertThat(wordCounts, empty());
    }

    @Test
    @DisplayName("Given word counts in store then wordcount query for all words returns list of word counts")
    void givenWordCountsInStoreThenWordCountQueryForAllWordsReturnsListOfWordCounts() {

        when(interactiveQueryService.getQueryableStore(eq(WORD_COUNT_STORE), any(QueryableStoreType.class))).thenReturn(readOnlyKeyValueStore);
        when(readOnlyKeyValueStore.approximateNumEntries()).thenReturn(3L);
        when(readOnlyKeyValueStore.all()).thenReturn(
                new DummyKeyValueIterator<>(
                        new KeyValue<>("one", 1L),
                        new KeyValue<>("two", 2L),
                        new KeyValue<>("three", 3L)
                ));

        List<WordCount> wordCounts = wordCountProcessor.wordCount();

        assertThat(wordCounts,
                contains(
                        new WordCount("one", 1L),
                        new WordCount("two", 2L),
                        new WordCount("three", 3L)
                ));
    }

    @Test
    @DisplayName("Given no store then wordcount query for a particular word throws NoStoreException")
    void givenNoStoreThenWordCountQueryForAParticularWordThrowsNoStoreException() {

        when(interactiveQueryService.getQueryableStore(eq(WORD_COUNT_STORE), any(QueryableStoreType.class))).thenReturn(null);

        assertThrows(NoStoreException.class, () -> wordCountProcessor.wordCount("one"));
    }

    @Test
    @DisplayName("Given word in store then wordcount query for a particular word returns count for the word")
    void givenWordInStoreThenWordCountQueryForAParticularWordReturnsWordCountForTheWord() {

        when(interactiveQueryService.getQueryableStore(eq(WORD_COUNT_STORE), any(QueryableStoreType.class))).thenReturn(readOnlyKeyValueStore);
        when(readOnlyKeyValueStore.get("one")).thenReturn(1L);

        WordCount wordCount = wordCountProcessor.wordCount("one");

        assertThat(wordCount, is(new WordCount("one", 1L)));
    }

    @Test
    @DisplayName("Given word not in store then wordcount query for a particular word returns zero count for the word")
    void givenWordNotInStoreThenWordCountQueryForAParticularWordReturnsZeroWordCountForTheWord() {

        when(interactiveQueryService.getQueryableStore(eq(WORD_COUNT_STORE), any(QueryableStoreType.class))).thenReturn(readOnlyKeyValueStore);
        when(readOnlyKeyValueStore.get("one")).thenReturn(null);

        WordCount wordCount = wordCountProcessor.wordCount("one");

        assertThat(wordCount, is(new WordCount("one", 0L)));
    }

    @Test
    @DisplayName("Given word in store then wordcount query for a particular word with different case returns count for the word")
    void givenWordInStoreThenWordCountQueryForAParticularWordWithDifferentCaseReturnsWordCountForTheWord() {

        when(interactiveQueryService.getQueryableStore(eq(WORD_COUNT_STORE), any(QueryableStoreType.class))).thenReturn(readOnlyKeyValueStore);
        when(readOnlyKeyValueStore.get("one")).thenReturn(1L);

        WordCount wordCount = wordCountProcessor.wordCount("ONE");

        assertThat(wordCount, is(new WordCount("one", 1L)));
    }

    @Test
    @DisplayName("Given no store then windowedwordcount query for all words throws NoStoreException")
    void givenNoStoreThenWindowedWordCountQueryForAllWordsThrowsNoStoreException() {

        when(interactiveQueryService.getQueryableStore(eq(WINDOWED_WORD_COUNT_STORE), any(QueryableStoreType.class))).thenReturn(null);

        assertThrows(NoStoreException.class, wordCountProcessor::windowedWordCount);
    }

    @Test
    @DisplayName("Given empty store for time period then windowedwordcount query for all words returns empty list")
    void givenEmptyStoreThenWindowedWordCountQueryForAllWordsReturnsEmptyList() {

        when(interactiveQueryService.getQueryableStore(eq(WINDOWED_WORD_COUNT_STORE), any(QueryableStoreType.class))).thenReturn(readOnlyWindowStore);
        when(readOnlyWindowStore.fetchAll(any(), any())).thenReturn(new DummyKeyValueIterator<>());

        List<WordCount> wordCounts = wordCountProcessor.windowedWordCount();

        assertThat(wordCounts, empty());
    }

    @Test
    @DisplayName("Given word counts in store for time period then windowedwordcount query for all words returns list of word counts")
    void givenWordCountsInStoreThenWindowedWordCountQueryForAllWordsReturnsListOfWordCounts() {

        when(interactiveQueryService.getQueryableStore(eq(WINDOWED_WORD_COUNT_STORE), any(QueryableStoreType.class))).thenReturn(readOnlyWindowStore);
        when(readOnlyWindowStore.fetchAll(any(), any())).thenReturn(
                new DummyKeyValueIterator<>(
                        new KeyValue<>(new Windowed<>("one", TIME_WINDOW), 1L),
                        new KeyValue<>(new Windowed<>("two", TIME_WINDOW), 2L),
                        new KeyValue<>(new Windowed<>("three", TIME_WINDOW), 3L)
                ));

        List<WordCount> wordCounts = wordCountProcessor.windowedWordCount();

        assertThat(wordCounts,
                contains(
                        new WordCount("one", 1L),
                        new WordCount("two", 2L),
                        new WordCount("three", 3L)
                ));
    }

    @Test
    @DisplayName("Given no store then windowedwordcount query for a particular word throws NoStoreException")
    void givenNoStoreThenWindowedWordCountQueryForAParticularWordThrowsNoStoreException() {

        when(interactiveQueryService.getQueryableStore(eq(WINDOWED_WORD_COUNT_STORE), any(QueryableStoreType.class))).thenReturn(null);

        assertThrows(NoStoreException.class, () -> wordCountProcessor.windowedWordCount("one"));
    }

    @Test
    @DisplayName("Given word in store for time period then windowedwordcount query for a particular word returns count for the word")
    void givenWordInStoreThenWindowedWordCountQueryForAParticularWordReturnsWordCountForTheWord() {

        when(interactiveQueryService.getQueryableStore(eq(WINDOWED_WORD_COUNT_STORE), any(QueryableStoreType.class))).thenReturn(readOnlyWindowStore);
        when(readOnlyWindowStore.fetch("one", WINDOW_DURATION_SECS)).thenReturn(1L);

        WordCount wordCount = wordCountProcessor.windowedWordCount("one");

        assertThat(wordCount, is(new WordCount("one", 1L)));
    }

    @Test
    @DisplayName("Given word not in store for time period then windowedwordcount query for a particular word returns zero count for the word")
    void givenWordNotInStoreThenWindowedWordCountQueryForAParticularWordReturnsZeroWordCountForTheWord() {

        when(interactiveQueryService.getQueryableStore(eq(WINDOWED_WORD_COUNT_STORE), any(QueryableStoreType.class))).thenReturn(readOnlyWindowStore);
        when(readOnlyWindowStore.fetch("one", WINDOW_DURATION_SECS)).thenReturn(null);

        WordCount wordCount = wordCountProcessor.windowedWordCount("one");

        assertThat(wordCount, is(new WordCount("one", 0L)));
    }

    @Test
    @DisplayName("Given word in store for time period then windowedwordcount query for a particular word with different case returns count for the word")
    void givenWordInStoreThenWindowedWordCountQueryForAParticularWordWithDifferentCaseReturnsWordCountForTheWord() {

        when(interactiveQueryService.getQueryableStore(eq(WINDOWED_WORD_COUNT_STORE), any(QueryableStoreType.class))).thenReturn(readOnlyWindowStore);
        when(readOnlyWindowStore.fetch("one", WINDOW_DURATION_SECS)).thenReturn(null);

        WordCount wordCount = wordCountProcessor.windowedWordCount("ONE");

        assertThat(wordCount, is(new WordCount("one", 0L)));
    }


    private class DummyKeyValueIterator<K, V> implements KeyValueIterator<K, V> {

        private KeyValue<K, V>[] keyValues;
        int idx = 0;

        DummyKeyValueIterator(KeyValue<K, V>... keyValues) {
            this.keyValues = keyValues;
        }

        @Override
        public void close() {

        }

        @Override
        public K peekNextKey() {
            return keyValues[idx].key;
        }

        @Override
        public boolean hasNext() {
            return idx < keyValues.length;
        }

        @Override
        public KeyValue<K, V> next() {
            return keyValues[idx++];
        }
    }
}
