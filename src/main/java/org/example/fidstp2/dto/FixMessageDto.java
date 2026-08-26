package org.example.fidstp2.dto;

import java.util.Map;
import java.util.Objects;

public record FixMessageDto(Map<String, String> tags) {
    public FixMessageDto {
        Objects.requireNonNull(tags, "tags are required");
        tags = Map.copyOf(tags);
    }

    public String get(String tag) {
        return tags.get(tag);
    }
}

