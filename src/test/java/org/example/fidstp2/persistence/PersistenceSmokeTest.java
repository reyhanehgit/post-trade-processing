package org.example.fidstp2.persistence;

import org.example.fidstp2.domain.BuySell;
import org.example.fidstp2.domain.OptionStyle;
import org.example.fidstp2.domain.OptionType;
import org.example.fidstp2.domain.ProcessingStatus;
import org.example.fidstp2.repository.OutboxEventRepository;
import org.example.fidstp2.repository.ProcessingHistoryRepository;
import org.example.fidstp2.repository.TradeLegRepository;
import org.example.fidstp2.repository.TradeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:fidstp2;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=true"
})
class PersistenceSmokeTest {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private TradeLegRepository tradeLegRepository;

    @Autowired
    private ProcessingHistoryRepository processingHistoryRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    void savesTradeHistoryAndOutboxRecords() {
        FxOptionTradeEntity trade = new FxOptionTradeEntity();
        trade.setTradeId("T-100");
        trade.setExternalTradeId("EXT-100");
        trade.setProductType("FX_OPTION");
        trade.setCurrencyPair("EUR/USD");
        trade.setBaseCurrency("EUR");
        trade.setQuoteCurrency("USD");
        trade.setNotionalAmount(new BigDecimal("1000000.0000"));
        trade.setNotionalCurrency("EUR");
        trade.setBuySell(BuySell.BUY);
        trade.setTradeDate(LocalDate.of(2026, 8, 26));
        trade.setValueDate(LocalDate.of(2026, 8, 29));
        trade.setCounterpartyId("CP-1");
        trade.setLegalEntityId("LE-1");
        trade.setSourceSystem("OMS");
        trade.setProcessingStatus(ProcessingStatus.RECEIVED);
        trade.setReceivedTimestamp(Instant.parse("2026-08-26T10:00:00Z"));
        trade.setOptionType(OptionType.CALL);
        trade.setStrikePrice(new BigDecimal("1.2500"));
        trade.setExpiryDate(LocalDate.of(2026, 10, 1));
        trade.setOptionStyle(OptionStyle.VANILLA);

        TradeLegEntity leg = new TradeLegEntity();
        leg.setLegId("L-1");
        leg.setCurrencyPair("EUR/USD");
        leg.setNotional(new BigDecimal("500000.0000"));
        leg.setStrikePrice(new BigDecimal("1.2500"));
        leg.setExpiryDate(LocalDate.of(2026, 10, 1));
        leg.setOptionType(OptionType.CALL);
        leg.setTrade(trade);
        trade.setLegs(List.of(leg));

        tradeRepository.save(trade);
        tradeLegRepository.save(leg);

        ProcessingHistoryEntity history = new ProcessingHistoryEntity();
        history.setTradeId("T-100");
        history.setStatus(ProcessingStatus.PROCESSED);
        history.setStage("PROCESSING");
        history.setMessage("saved");
        history.setCreatedAt(Instant.parse("2026-08-26T10:01:00Z"));
        processingHistoryRepository.save(history);

        OutboxEventEntity outbox = new OutboxEventEntity();
        outbox.setAggregateId("T-100");
        outbox.setEventType("ProcessedTradeEvent");
        outbox.setPayload("{\"tradeId\":\"T-100\"}");
        outbox.setStatus("NEW");
        outbox.setCreatedAt(Instant.parse("2026-08-26T10:02:00Z"));
        outboxEventRepository.save(outbox);

        assertEquals("T-100", tradeRepository.findByTradeId("T-100").orElseThrow().getTradeId());
        assertEquals(1, tradeLegRepository.findByTrade_TradeId("T-100").size());
        assertEquals(1, processingHistoryRepository.findByTradeIdOrderByCreatedAtAsc("T-100").size());
        assertEquals(1, outboxEventRepository.findByStatusOrderByCreatedAtAsc("NEW").size());
    }
}

