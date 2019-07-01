package playproject.springkafkawordcount.wordcount;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import playproject.springkafkawordcount.wordcount.spring.WordCountProcessorBinding;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableBinding({WordCountProcessorBinding.class})
public class WordCountProcessorApplication {

    public static void main(String[] args) {
        run(WordCountProcessorApplication.class, args);
    }
}
