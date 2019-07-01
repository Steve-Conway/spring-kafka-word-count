package playproject.springkafkawordcount.infrastructure.model.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import playproject.springkafkawordcount.infrastructure.model.WordCount;

@Value
public class WordCountEvent implements Event<WordCount> {

    private final WordCount payload;

    @JsonCreator
    public WordCountEvent(@JsonProperty("payload") WordCount payload) {
        this.payload = payload;
    }
}
