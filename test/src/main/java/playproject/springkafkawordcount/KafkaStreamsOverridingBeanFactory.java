package playproject.springkafkawordcount;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.CleanupConfig;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class KafkaStreamsOverridingBeanFactory extends DefaultListableBeanFactory {

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException {
        BeanDefinition def = beanDefinition;
        if(StreamsBuilderFactoryBean.class.getName().equalsIgnoreCase(beanDefinition.getBeanClassName())){
            Supplier<StreamsBuilderFactoryBean> instanceSupplier = (Supplier<StreamsBuilderFactoryBean>)((AbstractBeanDefinition)beanDefinition).getInstanceSupplier();
            def = BeanDefinitionBuilder.genericBeanDefinition(StreamsBuilderFactoryBean.class, () -> {
                StreamsBuilderFactoryBean streamsBuilderFactoryBean = instanceSupplier.get();
                KafkaStreamsConfiguration streamsConfig = new KafkaStreamsConfiguration(streamsBuilderFactoryBean.getStreamsConfiguration().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue)));
                CleanupConfig cleanupConfig = new CleanupConfig();
                StreamsBuilderFactoryBeanDecorator streamsBuilderFactoryBeanDecorator = new StreamsBuilderFactoryBeanDecorator(streamsConfig,cleanupConfig);
                streamsBuilderFactoryBeanDecorator.setAutoStartup(false);
                return streamsBuilderFactoryBeanDecorator;
            })
                    .getRawBeanDefinition();

        }
        super.registerBeanDefinition(beanName,def);
    }
}
