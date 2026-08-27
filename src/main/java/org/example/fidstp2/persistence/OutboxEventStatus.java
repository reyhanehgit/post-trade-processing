package org.example.fidstp2.persistence;

public enum OutboxEventStatus {
    NEW,
    PUBLISHED,
    PENDING_RETRY,
    DEAD_LETTERED;

    public String value() {
        return name();
    }
}

