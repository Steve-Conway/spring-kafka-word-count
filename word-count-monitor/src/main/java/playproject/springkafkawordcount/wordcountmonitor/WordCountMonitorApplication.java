package playproject.springkafkawordcount.wordcountmonitor;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import playproject.springkafkawordcount.wordcountmonitor.spring.WordCountMonitorBinding;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableBinding({WordCountMonitorBinding.class})
public class WordCountMonitorApplication {

    public static void main(String[] args) {
        run(WordCountMonitorApplication.class, args);
    }

}
