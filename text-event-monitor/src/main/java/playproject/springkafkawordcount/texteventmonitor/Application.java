package playproject.springkafkawordcount.texteventmonitor;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import playproject.springkafkawordcount.texteventmonitor.spring.TextEventMonitorBinding;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableBinding({TextEventMonitorBinding.class})
public class Application {

    public static void main(String[] args) {
        run(Application.class, args);
    }

}
