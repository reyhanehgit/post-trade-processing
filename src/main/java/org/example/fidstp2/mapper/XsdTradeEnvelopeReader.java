package org.example.fidstp2.mapper;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.example.fidstp2.schema.trade.ObjectFactory;
import org.example.fidstp2.schema.trade.TradeEnvelopeType;

import java.io.InputStream;

public class XsdTradeEnvelopeReader {
    private final Unmarshaller unmarshaller;

    public XsdTradeEnvelopeReader() {
        try {
            JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
            this.unmarshaller = context.createUnmarshaller();
        } catch (JAXBException ex) {
            throw new IllegalStateException("unable to initialize JAXB unmarshaller", ex);
        }
    }

    public TradeEnvelopeType read(InputStream xml) {
        if (xml == null) {
            throw new IllegalArgumentException("xml input is required");
        }
        try {
            Object result = unmarshaller.unmarshal(xml);
            if (result instanceof JAXBElement<?> element && element.getValue() instanceof TradeEnvelopeType envelope) {
                return envelope;
            }
            if (result instanceof TradeEnvelopeType envelope) {
                return envelope;
            }
            throw new IllegalArgumentException("xml is not a tradeEnvelope document");
        } catch (JAXBException ex) {
            throw new IllegalArgumentException("failed to parse tradeEnvelope xml", ex);
        }
    }
}

