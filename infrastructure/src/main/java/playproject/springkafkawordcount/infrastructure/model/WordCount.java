package playproject.springkafkawordcount.infrastructure.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WordCount {
    private String word;
    private long count;
//    private Date start;
//    private Date end;
}
