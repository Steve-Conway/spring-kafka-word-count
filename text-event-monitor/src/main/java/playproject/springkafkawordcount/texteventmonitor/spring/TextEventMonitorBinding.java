package playproject.springkafkawordcount.texteventmonitor.spring;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface TextEventMonitorBinding {

    @Input("text-events")
    MessageChannel textEvents();
}
