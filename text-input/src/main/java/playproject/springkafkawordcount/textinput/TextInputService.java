package playproject.springkafkawordcount.textinput;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import playproject.springkafkawordcount.infrastructure.model.event.TextEvent;

@RequestMapping
@Slf4j
public class TextInputService {

    private final MessageChannel textEvents;

    public TextInputService(MessageChannel textEvents) {
        this.textEvents = textEvents;
    }

    @PostMapping("/textinput")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void textInput(@RequestParam String text) throws EmptyTextInputException {
        log.info("Text input: " + ((text == null) ? "<null>" : "\"" + text + "\""));
        if (text == null || text.isBlank()) {
            throw new EmptyTextInputException();
        }
        textEvents.send(new GenericMessage<>(new TextEvent(text)));
    }

    @ResponseStatus(value=HttpStatus.BAD_REQUEST,reason="Empty text input is not valid")
    public class EmptyTextInputException extends Exception
    {
        private static final long serialVersionUID = 100L;
    }

}
