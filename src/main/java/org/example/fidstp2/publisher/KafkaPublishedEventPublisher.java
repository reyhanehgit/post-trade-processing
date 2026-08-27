package org.example.fidstp2.publisher;

import org.example.fidstp2.dto.ProcessedTradeEvent;
import org.example.fidstp2.exception.PublishingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Objects;

public class KafkaPublishedEventPublisher implements PublishedEventPublisher<ProcessedTradeEvent> {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;
    private final ObjectMapper objectMapper;

    public KafkaPublishedEventPublisher(KafkaTemplate<String, String> kafkaTemplate, String topic, ObjectMapper objectMapper) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate, "kafkaTemplate is required");
        this.topic = Objects.requireNonNull(topic, "topic is required");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper is required");
    }

    @Override
    public void publish(String key, ProcessedTradeEvent event) {
        try {
            kafkaTemplate.send(topic, key, objectMapper.writeValueAsString(event)).join();
        } catch (JsonProcessingException ex) {
            throw new PublishingException("failed to serialize trade event for topic " + topic, ex);
        } catch (RuntimeException ex) {
            throw new PublishingException("failed to publish trade event to topic " + topic, ex);
        }
    }
}

