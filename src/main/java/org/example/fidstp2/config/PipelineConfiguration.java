package org.example.fidstp2.config;

import org.example.fidstp2.domain.CurrencyPair;
import org.example.fidstp2.domain.LegalEntity;
import org.example.fidstp2.domain.SettlementInstruction;
import org.example.fidstp2.dto.ProcessedTradeEvent;
import org.example.fidstp2.enrichment.CounterpartyService;
import org.example.fidstp2.enrichment.CurrencyPairService;
import org.example.fidstp2.enrichment.FxOptionTradeEnrichmentService;
import org.example.fidstp2.enrichment.InMemoryCurrencyPairService;
import org.example.fidstp2.enrichment.InMemoryLegalEntityService;
import org.example.fidstp2.enrichment.InMemorySettlementInstructionService;
import org.example.fidstp2.enrichment.JpaCounterpartyService;
import org.example.fidstp2.enrichment.RemoteCounterpartyService;
import org.example.fidstp2.enrichment.LegalEntityService;
import org.example.fidstp2.enrichment.SettlementInstructionService;
import org.example.fidstp2.mapper.FixToFxOptionTradeMapper;
import org.example.fidstp2.mapper.EnrichedTradeToFxOptionTradeEntityMapper;
import org.example.fidstp2.mapper.ProcessedTradeToPublishedEventMapper;
import org.example.fidstp2.parser.DefaultFixMessageAdapter;
import org.example.fidstp2.parser.FixMessageAdapter;
import org.example.fidstp2.parser.FixTradeMessageParser;
import org.example.fidstp2.processor.FxOptionTradeProcessor;
import org.example.fidstp2.publisher.KafkaPublishedEventPublisher;
import org.example.fidstp2.publisher.PublishedEventPublisher;
import org.example.fidstp2.service.TradePipelineService;
import org.example.fidstp2.service.TradePersistenceService;
import org.example.fidstp2.service.TradeProcessingService;
import org.example.fidstp2.service.TradePublicationService;
import org.example.fidstp2.service.CounterpartyQueryService;
import org.example.fidstp2.repository.CounterpartyRepository;
import org.example.fidstp2.repository.OutboxEventRepository;
import org.example.fidstp2.repository.ProcessingHistoryRepository;
import org.example.fidstp2.repository.TradeRepository;
import org.example.fidstp2.validator.FxOptionTradeValidator;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.HashMap;
import java.time.Clock;
import java.util.Map;

@Configuration
@EnableKafka
public class PipelineConfiguration {

    @Bean
    public FixMessageAdapter fixMessageAdapter() {
        return new DefaultFixMessageAdapter();
    }

    @Bean
    public FixToFxOptionTradeMapper fixToFxOptionTradeMapper() {
        return new FixToFxOptionTradeMapper();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    @Bean
    public FixTradeMessageParser tradeMessageParser(FixMessageAdapter adapter, FixToFxOptionTradeMapper mapper) {
        return new FixTradeMessageParser(adapter, mapper);
    }

    @Bean
    public FxOptionTradeValidator tradeValidator() {
        return new FxOptionTradeValidator();
    }

    @Bean
    public CounterpartyService counterpartyService(CounterpartyRepository counterpartyRepository) {
        return new JpaCounterpartyService(counterpartyRepository);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CounterpartyQueryService counterpartyQueryService(CounterpartyService counterpartyService) {
        return new CounterpartyQueryService(counterpartyService);
    }

    @Bean
    public CurrencyPairService currencyPairService() {
        return new InMemoryCurrencyPairService(Map.of(
                "EUR/USD", new CurrencyPair("EUR/USD", "EUR", "USD")
        ));
    }

    @Bean
    public LegalEntityService legalEntityService() {
        return new InMemoryLegalEntityService(Map.of(
                "LE-1", new LegalEntity("LE-1", "Demo Legal Entity", "EMEA")
        ));
    }

    @Bean
    public SettlementInstructionService settlementInstructionService() {
        return new InMemorySettlementInstructionService(Map.of(
                "CP-1", new SettlementInstruction("SI-1", "ACC-001", "CLS")
        ));
    }

    @Bean
    public FxOptionTradeEnrichmentService fxOptionTradeEnrichmentService(
            CounterpartyService counterpartyService,
            CurrencyPairService currencyPairService,
            LegalEntityService legalEntityService,
            SettlementInstructionService settlementInstructionService
    ) {
        return new FxOptionTradeEnrichmentService(
                counterpartyService,
                currencyPairService,
                legalEntityService,
                settlementInstructionService
        );
    }

    @Bean
    public FxOptionTradeProcessor fxOptionTradeProcessor() {
        return new FxOptionTradeProcessor();
    }

    @Bean
    public EnrichedTradeToFxOptionTradeEntityMapper enrichedTradeToFxOptionTradeEntityMapper() {
        return new EnrichedTradeToFxOptionTradeEntityMapper();
    }

    @Bean
    public TradePersistenceService tradePersistenceService(
            TradeRepository tradeRepository,
            ProcessingHistoryRepository processingHistoryRepository,
            OutboxEventRepository outboxEventRepository,
            EnrichedTradeToFxOptionTradeEntityMapper mapper
    ) {
        return new TradePersistenceService(
                tradeRepository,
                processingHistoryRepository,
                outboxEventRepository,
                mapper,
                Clock.systemUTC()
        );
    }

    @Bean
    public TradeProcessingService tradeProcessingService(
            FixTradeMessageParser tradeMessageParser,
            FxOptionTradeValidator tradeValidator,
            FxOptionTradeEnrichmentService enrichmentService,
            FxOptionTradeProcessor processor,
            TradePersistenceService persistenceService
    ) {
        return new TradeProcessingService(tradeMessageParser, tradeValidator, enrichmentService, processor, persistenceService);
    }

    @Bean
    public ProcessedTradeToPublishedEventMapper processedTradeToPublishedEventMapper() {
        return new ProcessedTradeToPublishedEventMapper(Clock.systemUTC());
    }

    @Bean
    public ProducerFactory<String, String> producerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
    ) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringSerializer.class.getName());
        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public PublishedEventPublisher<ProcessedTradeEvent> publishedEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.kafka.outbound-topic}") String outboundTopic,
            ObjectMapper objectMapper
    ) {
        return new KafkaPublishedEventPublisher(kafkaTemplate, outboundTopic, objectMapper);
    }

    @Bean
    public TradePublicationService tradePublicationService(
            ProcessedTradeToPublishedEventMapper mapper,
            PublishedEventPublisher<ProcessedTradeEvent> publisher,
            TradePersistenceService persistenceService,
            ObjectMapper objectMapper
    ) {
        return new TradePublicationService(mapper, publisher, persistenceService, objectMapper);
    }

    @Bean
    public TradePipelineService tradePipelineService(
            TradeProcessingService tradeProcessingService,
            TradePublicationService tradePublicationService
    ) {
        return new TradePipelineService(tradeProcessingService, tradePublicationService);
    }
}

