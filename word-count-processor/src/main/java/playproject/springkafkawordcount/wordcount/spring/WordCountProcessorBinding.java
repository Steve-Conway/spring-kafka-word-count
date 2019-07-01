package playproject.springkafkawordcount.wordcount.spring;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import playproject.springkafkawordcount.infrastructure.model.event.TextEvent;
import playproject.springkafkawordcount.infrastructure.model.event.WordCountEvent;

public interface WordCountProcessorBinding {

    @Input("text-input")
    KStream<?, TextEvent> textInput();

    @Input("text-input-for-windowed")
    KStream<?, TextEvent> textInputForWindowed();

    @Output("word-count")
    KStream<?, WordCountEvent> wordCount();

    @Output("windowed-word-count")
    KStream<?, WordCountEvent> windowedWordCount();
}
