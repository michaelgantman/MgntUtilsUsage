package com.example.stamboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.undercouch.bson4jackson.BsonFactory;
import de.undercouch.bson4jackson.BsonGenerator;

@Configuration
public class BsonConfig {

    @Bean
    public ObjectMapper bsonObjectMapper() {
        BsonFactory fac = new BsonFactory();
        fac.enable(BsonGenerator.Feature.ENABLE_STREAMING);
        return new ObjectMapper(fac);
    }
}
