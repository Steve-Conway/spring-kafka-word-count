package playproject.springkafkawordcount;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.CleanupConfig;
import org.springframework.util.Assert;

import java.io.File;

@Slf4j
public class StreamsBuilderFactoryBeanDecorator extends StreamsBuilderFactoryBean {

    public StreamsBuilderFactoryBeanDecorator() {
        super();
    }

    public StreamsBuilderFactoryBeanDecorator(KafkaStreamsConfiguration streamsConfig, CleanupConfig cleanupConfig) {
        super(streamsConfig, cleanupConfig);
    }

    public StreamsBuilderFactoryBeanDecorator(KafkaStreamsConfiguration streamsConfig) {
        super(streamsConfig);
    }

    @Override
    protected StreamsBuilder createInstance() {
        if (this.isAutoStartup()) {
            Assert.state(this.getStreamsConfig() != null || this.getStreamsConfiguration() != null,
                    "'streamsConfig' or streams configuration properties must not be null");
        }
        return new StreamsBuilderPropertiesOverridingDecorator();
    }

    @Override
    public synchronized void stop() {
        super.stop();
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            log.info("WINDOWS OS MODE - Cleanup state store.");
            try {
                String applicationId = getStreamsConfiguration().getProperty(StreamsConfig.APPLICATION_ID_CONFIG);
                String tmpDir = getStreamsConfiguration().getProperty(StreamsConfig.STATE_DIR_CONFIG);
                FileUtils.deleteQuietly(new File(tmpDir,applicationId));
            } catch(Exception e) {
                log.error("could not stop streams",e);
            }
        }
    }
}
