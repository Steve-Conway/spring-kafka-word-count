package playproject.springkafkawordcount.textinput.spring;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface TextInputServiceBinding {

    @Output("text-events")
    MessageChannel textEvents();
}
