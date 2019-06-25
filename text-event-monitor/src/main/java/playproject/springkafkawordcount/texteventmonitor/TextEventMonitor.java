package playproject.springkafkawordcount.texteventmonitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;
import playproject.springkafkawordcount.infrastructure.model.event.TextEvent;

@Slf4j
public class TextEventMonitor {

    @StreamListener("text-events")
    public void process(TextEvent textEvent) {
        log.info("Received text event: " + textEvent);
    }
}
