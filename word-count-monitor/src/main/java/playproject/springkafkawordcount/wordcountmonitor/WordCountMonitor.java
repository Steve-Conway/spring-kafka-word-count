package playproject.springkafkawordcount.wordcountmonitor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.StreamListener;
import playproject.springkafkawordcount.infrastructure.model.WordCount;

@Slf4j
public class WordCountMonitor {

    @StreamListener("word-count")
    public void process(KStream<String, WordCount> wordCount) {
        wordCount.foreach((key, value) ->
                log.info("Received word count: " + value)
        );
    }

    @StreamListener("windowed-word-count")
    public void processWindowed(KStream<String, WordCount> wordCount) {
        wordCount.foreach((key, value) ->
                log.info("Received windowed word count: " + value)
        );
    }
}
