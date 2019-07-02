package playproject.springkafkawordcount.wordcount.spring;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import playproject.springkafkawordcount.wordcount.WordCountProcessor;

@Configuration
@EnableConfigurationProperties(WordCountProcessorProperties.class)
public class WordCountProcessorConfiguration {

    @Bean
    public WordCountProcessor wordCountProcessor(WordCountProcessorProperties properties, InteractiveQueryService interactiveQueryService) {
        return new WordCountProcessor(properties.getWindowDurationSecs(), interactiveQueryService);
    }
}
