package playproject.springkafkawordcount.wordcountmonitor.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import playproject.springkafkawordcount.wordcountmonitor.WordCountMonitor;

@Configuration
public class WordCountMonitorConfiguration {

    @Bean
    public WordCountMonitor wordCountMonitor() {
        return new WordCountMonitor();
    }
}
