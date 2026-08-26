package org.example.fidstp2.publisher;

import org.example.fidstp2.dto.ProcessedTradeEvent;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;

import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KafkaPublishedEventPublisherTest {

    @Test
    void publishesEventToConfiguredTopicWithTradeIdKey() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        KafkaPublishedEventPublisher publisher = new KafkaPublishedEventPublisher(
                kafkaTemplate,
                "fx.option.trade.processed",
                new ObjectMapper().registerModule(new JavaTimeModule())
        );
        ProcessedTradeEvent event = new ProcessedTradeEvent("1.0", "T-901", "PROCESSED", Instant.now());

        publisher.publish("T-901", event);

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(org.mockito.ArgumentMatchers.eq("fx.option.trade.processed"), org.mockito.ArgumentMatchers.eq("T-901"), payloadCaptor.capture());

        assertTrue(payloadCaptor.getValue().contains("\"eventVersion\":\"1.0\""));
        assertTrue(payloadCaptor.getValue().contains("\"tradeId\":\"T-901\""));
        assertTrue(payloadCaptor.getValue().contains("\"status\":\"PROCESSED\""));
    }
}

