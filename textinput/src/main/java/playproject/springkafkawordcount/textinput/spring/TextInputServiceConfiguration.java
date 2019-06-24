package playproject.springkafkawordcount.textinput.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import playproject.springkafkawordcount.textinput.TextInputService;

@Configuration
public class TextInputServiceConfiguration {

    @Bean
    public TextInputService textInputService() {
        return new TextInputService();
    }
}
