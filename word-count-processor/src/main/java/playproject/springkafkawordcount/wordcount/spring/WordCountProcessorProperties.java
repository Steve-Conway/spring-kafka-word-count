package playproject.springkafkawordcount.wordcount.spring;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "word-count-processor")
@Data
public class WordCountProcessorProperties {

    private int windowDurationSecs = 60;
}
