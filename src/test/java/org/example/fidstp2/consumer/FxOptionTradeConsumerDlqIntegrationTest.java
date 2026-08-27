package org.example.fidstp2.consumer;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.fidstp2.service.TradePipelineService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "app.kafka.inbound-topic=fx.option.trade.raw.it",
        "app.kafka.dlq-topic=fx.option.trade.dlq.it",
        "app.kafka.retry.max-attempts=2",
        "app.kafka.retry.backoff-ms=50",
        "app.outbox.retry.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:fidstp2_dlq_it;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=true"
})
@EmbeddedKafka(partitions = 1, topics = {"fx.option.trade.raw.it", "fx.option.trade.dlq.it"})
class FxOptionTradeConsumerDlqIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.inbound-topic}")
    private String inboundTopic;

    @Value("${app.kafka.dlq-topic}")
    private String dlqTopic;

    private Consumer<String, String> dlqConsumer;

    @AfterEach
    void tearDown() {
        if (dlqConsumer != null) {
            dlqConsumer.close();
        }
    }

    @Test
    void sendsFailedInboundMessageToDlqAfterConfiguredRetries() {
        dlqConsumer = createDlqConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(dlqConsumer, dlqTopic);

        String rawMessage = "11=T-DLQ-1|37=EXT-DLQ-1|55=EUR/USD|";
        kafkaTemplate.send(inboundTopic, rawMessage).join();

        ConsumerRecord<String, String> dlqRecord = KafkaTestUtils.getSingleRecord(dlqConsumer, dlqTopic, Duration.ofSeconds(10));
        assertEquals(rawMessage, dlqRecord.value());
    }

    private Consumer<String, String> createDlqConsumer() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("dlq-it-group", "false", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        ConsumerFactory<String, String> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), new StringDeserializer());
        return consumerFactory.createConsumer();
    }

    @TestConfiguration
    static class FailingPipelineConfig {
        @Bean
        @Primary
        TradePipelineService failingTradePipelineService() {
            TradePipelineService tradePipelineService = Mockito.mock(TradePipelineService.class);
            Mockito.when(tradePipelineService.handleRawMessage(Mockito.anyString()))
                    .thenThrow(new RuntimeException("forced failure for DLQ integration test"));
            return tradePipelineService;
        }
    }
}

