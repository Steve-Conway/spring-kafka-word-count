package playproject.springkafkawordcount.textinput.spring;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessageChannel;
import playproject.springkafkawordcount.textinput.TextInputService;

@Configuration
public class TextInputServiceConfiguration {

    @Bean
    public TextInputService textInputService(@Qualifier("text-input") MessageChannel textInput) {
        return new TextInputService(textInput);
    }
}
