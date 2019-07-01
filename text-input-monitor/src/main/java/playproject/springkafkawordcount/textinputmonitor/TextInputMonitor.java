package playproject.springkafkawordcount.textinputmonitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;
import playproject.springkafkawordcount.infrastructure.model.event.TextEvent;

@Slf4j
public class TextInputMonitor {

    @StreamListener("text-input")
    public void process(TextEvent text) {
        log.info("Received text input: " + text.getPayload());
    }
}
