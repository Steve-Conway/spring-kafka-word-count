package playproject.springkafkawordcount;

import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;

public class KafkaStreamsOverridingContext extends AnnotationConfigServletWebServerApplicationContext {

    public KafkaStreamsOverridingContext() {
        super(new KafkaStreamsOverridingBeanFactory());
    }

}
