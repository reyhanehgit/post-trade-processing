package org.example.fidstp2.enrichment;

import org.example.fidstp2.domain.LegalEntity;
import org.example.fidstp2.exception.ReferenceDataNotFoundException;

import java.util.Map;

public class InMemoryLegalEntityService implements LegalEntityService {
    private final Map<String, LegalEntity> legalEntities;

    public InMemoryLegalEntityService(Map<String, LegalEntity> legalEntities) {
        this.legalEntities = Map.copyOf(legalEntities);
    }

    @Override
    public LegalEntity getLegalEntity(String id) {
        LegalEntity legalEntity = legalEntities.get(id);
        if (legalEntity == null) {
            throw new ReferenceDataNotFoundException("legal entity not found: " + id);
        }
        return legalEntity;
    }
}

