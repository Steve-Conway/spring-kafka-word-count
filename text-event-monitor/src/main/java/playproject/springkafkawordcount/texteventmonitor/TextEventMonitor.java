package playproject.springkafkawordcount.texteventmonitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;

@Slf4j
public class TextEventMonitor {

    @StreamListener("text-events")
    public void process(String text) {
        log.info("Received text event: " + text);
    }
}
