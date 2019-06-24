package playproject.springkafkawordcount.textinput;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequestMapping
@Slf4j
public class TextInputService {

    @PostMapping("/textinput")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void textInput(@RequestParam String text) {
        log.info(text);
    }
}
