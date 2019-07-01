package playproject.springkafkawordcount;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.stream.annotation.StreamMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MessageConverter;

@Configuration
public class KafkaNullConfiguration {

    @Bean
    @StreamMessageConverter
    @ConditionalOnMissingBean(KafkaNullConverter.class)
    MessageConverter kafkaNullConverter() {
        return new KafkaNullConverter();
    }
}
