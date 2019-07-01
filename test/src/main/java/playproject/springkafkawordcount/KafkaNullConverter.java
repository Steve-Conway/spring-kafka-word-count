package playproject.springkafkawordcount;

import org.springframework.kafka.support.KafkaNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.AbstractMessageConverter;

import java.util.Collections;

public class KafkaNullConverter extends AbstractMessageConverter {

    public KafkaNullConverter() {
        super(Collections.emptyList());
    }

    @Override
    protected boolean supports(Class<?> aClass) {
        return KafkaNull.class.equals(aClass);
    }

    @Override
    protected boolean canConvertFrom(Message<?> message, Class<?> targetClass) {
        return message.getPayload() instanceof KafkaNull;
    }

    @Override
    protected Object convertFromInternal(Message<?> message, Class<?> targetClass,
                                         Object conversionHint) {
        return message.getPayload();
    }

    @Override
    protected Object convertToInternal(Object payload, MessageHeaders headers,
                                       Object conversionHint) {
        return payload;
    }

}
