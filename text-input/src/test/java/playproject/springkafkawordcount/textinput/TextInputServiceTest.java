package playproject.springkafkawordcount.textinput;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("Text input service tests")
@ExtendWith(MockitoExtension.class)
class TextInputServiceTest {

    @Mock
    private MessageChannel textInputChannel;

    @Captor
    private ArgumentCaptor<GenericMessage<String>> messageCaptor;

    @Test
    @DisplayName("Text input accepts text")
    void givenSomeTextInputThenTextInputAcceptsIt() throws TextInputService.EmptyTextInputException {

        TextInputService textInputService = new TextInputService(textInputChannel);

        textInputService.textInput("one two");

        verify(textInputChannel).send(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getPayload(), is("one two"));
    }

    @Test
    @DisplayName("Text input rejects null")
    void givenNullInputThenTextInputRejectsIt() {
        MessageChannel textInputChannel = mock(MessageChannel.class);
        TextInputService textInputService = new TextInputService(textInputChannel);

        assertThrows(TextInputService.EmptyTextInputException.class, () -> textInputService.textInput(null));
    }

    @Test
    @DisplayName("Text input rejects empty string")
    void givenEmptyInputThenTextInputRejectsIt() {
        MessageChannel textInputChannel = mock(MessageChannel.class);
        TextInputService textInputService = new TextInputService(textInputChannel);

        assertThrows(TextInputService.EmptyTextInputException.class, () -> textInputService.textInput("")) ;
    }

    @Test
    @DisplayName("Text input rejects blank string")
    void givenBlankInputThenTextInputRejectsIt() {
        MessageChannel textInputChannel = mock(MessageChannel.class);
        TextInputService textInputService = new TextInputService(textInputChannel);

        assertThrows(TextInputService.EmptyTextInputException.class, () -> textInputService.textInput("    "));
    }
}
