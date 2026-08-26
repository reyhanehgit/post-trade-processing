package org.example.fidstp2.publisher;

public interface PublishedEventPublisher<T> {
    void publish(String key, T event);
}

