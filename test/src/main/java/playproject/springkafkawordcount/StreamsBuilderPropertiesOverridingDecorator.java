package playproject.springkafkawordcount;

import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class StreamsBuilderPropertiesOverridingDecorator extends StreamsBuilder {

    private static final String REGEX_PREFIX = "<REGEX>";

    @Override
    public synchronized <K, V> KTable<K, V> table(String topic
            , Consumed<K, V> consumed
            , Materialized<K, V, KeyValueStore<Bytes, byte[]>> materialized) {
        return super.table(topic, consumed, (Materialized<K, V, KeyValueStore<Bytes, byte[]>>)new MaterializedWrapper(materialized).withLoggingDisabled());
    }

    @Override
    public synchronized <K, V> KStream<K, V> stream(Collection<String> topics, Consumed<K, V> consumed) {
        Pattern pattern = getPattern(topics);
        if(pattern != null){
            return stream(pattern,consumed);
        } else{
            return super.stream(topics, consumed);
        }

    }

    private Pattern getPattern(Collection<String> topics) {
        if(topics.size() == 1){
            String pattern = List.copyOf(topics).get(0);
            if(pattern.startsWith(REGEX_PREFIX)){
                pattern = pattern.substring(REGEX_PREFIX.length());
                return Pattern.compile(pattern);
            }
        }
        return null;
    }

    private static class MaterializedWrapper  extends Materialized<Bytes, byte[], KeyValueStore<Bytes, byte[]>> {

        protected MaterializedWrapper(Materialized materialized) {
            super(materialized);
            this.storeSupplier = Stores.inMemoryKeyValueStore(storeName);
        }
    }
}
