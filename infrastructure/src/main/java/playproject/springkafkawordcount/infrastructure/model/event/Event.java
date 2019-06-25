package playproject.springkafkawordcount.infrastructure.model.event;

public interface Event<T> {

    T getPayload();
}
