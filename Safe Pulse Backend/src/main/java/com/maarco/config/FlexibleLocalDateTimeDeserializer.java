package com.maarco.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    private static final DateTimeFormatter[] FORMATTERS = {
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,          // "2025-05-02T04:54:28"
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") // "2025-05-02 04:54:28"
    };

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateStr = p.getValueAsString();

        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDateTime.parse(dateStr, formatter);
            } catch (DateTimeParseException ignored) {
                // Try next format
            }
        }
        throw new IllegalArgumentException("Invalid date format: " + dateStr);
    }
}