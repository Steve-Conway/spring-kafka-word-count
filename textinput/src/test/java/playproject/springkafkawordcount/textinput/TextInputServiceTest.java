package playproject.springkafkawordcount.textinput;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Text input service tests")
class TextInputServiceTest {

    @Test
    @DisplayName("Text input accepts text")
    void givenSomeTextInputThenTextInputAcceptsIt() {
        TextInputService textInputService = new TextInputService();

        textInputService.textInput("some text");
    }

    @Test
    @DisplayName("Text input accepts empty string")
    void givenEmptyInputThenTextInputAcceptsIt() {
        TextInputService textInputService = new TextInputService();

        textInputService.textInput("");
    }

    @Test
    @DisplayName("Text input accepts null")
    void givenNullInputThenTextInputAcceptsIt() {
        TextInputService textInputService = new TextInputService();

        textInputService.textInput(null);
    }
}
