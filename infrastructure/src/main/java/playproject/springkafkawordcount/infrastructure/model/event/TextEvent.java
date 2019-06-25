package playproject.springkafkawordcount.infrastructure.model.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;

@Value
public class TextEvent implements Event<String> {

    @JsonCreator
    public TextEvent(@JsonProperty("payload") String payload) {
        this.payload = payload;
    }

    @NonNull
    private final String payload;
}
