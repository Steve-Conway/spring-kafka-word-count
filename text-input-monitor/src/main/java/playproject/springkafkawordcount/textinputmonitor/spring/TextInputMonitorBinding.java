package playproject.springkafkawordcount.textinputmonitor.spring;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface TextInputMonitorBinding {

    @Input("text-input")
    SubscribableChannel textInput();
}
