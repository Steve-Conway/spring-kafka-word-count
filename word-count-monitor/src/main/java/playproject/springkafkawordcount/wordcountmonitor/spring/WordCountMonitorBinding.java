package playproject.springkafkawordcount.wordcountmonitor.spring;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.Input;
import playproject.springkafkawordcount.infrastructure.model.event.WordCountEvent;

public interface WordCountMonitorBinding {

    @Input("word-count")
    KStream<?, WordCountEvent> wordCount();

    @Input("windowed-word-count")
    KStream<?, WordCountEvent> windowedWordCount();
}
