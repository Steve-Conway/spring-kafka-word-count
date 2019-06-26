package playproject.springkafkawordcount.texteventmonitor.spring;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface TextEventMonitorBinding {

    @Input("text-events")
    SubscribableChannel textEvents();
}
