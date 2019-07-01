package playproject.springkafkawordcount.textinputmonitor.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import playproject.springkafkawordcount.textinputmonitor.TextInputMonitor;

@Configuration
public class TextInputMonitorConfiguration {

    @Bean
    public TextInputMonitor textEventMonitor() {
        return new TextInputMonitor();
    }
}
