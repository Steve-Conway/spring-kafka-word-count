package playproject.springkafkawordcount.textinput;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import playproject.springkafkawordcount.textinput.spring.TextInputServiceBinding;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableBinding({TextInputServiceBinding.class})
public class Application {

    public static void main(String[] args) {
        run(Application.class, args);
    }

}
