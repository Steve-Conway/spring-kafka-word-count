package playproject.springkafkawordcount.texteventmonitor.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import playproject.springkafkawordcount.texteventmonitor.TextEventMonitor;

@Configuration
public class TextEventMonitorConfiguration {

    @Bean
    public TextEventMonitor textEventMonitor() {
        return new TextEventMonitor();
    }
}
