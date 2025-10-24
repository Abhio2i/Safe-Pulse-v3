package com.maarco.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        module.addDeserializer(LocalDateTime.class, new FlexibleLocalDateTimeDeserializer());
        mapper.registerModule(module);
        return mapper;
    }
}