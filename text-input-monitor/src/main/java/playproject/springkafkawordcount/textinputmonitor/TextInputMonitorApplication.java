package playproject.springkafkawordcount.textinputmonitor;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import playproject.springkafkawordcount.textinputmonitor.spring.TextInputMonitorBinding;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableBinding({TextInputMonitorBinding.class})
public class TextInputMonitorApplication {

    public static void main(String[] args) {
        run(TextInputMonitorApplication.class, args);
    }

}
