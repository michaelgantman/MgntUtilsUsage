package com.example.stamboot.config;

import com.mgnt.lifecycle.management.backgroundrunner.BackgroundThreadsRunner;
import de.undercouch.bson4jackson.BsonFactory;
import de.undercouch.bson4jackson.BsonGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class BsonConfig {

    @Bean
    public ObjectMapper bsonObjectMapper() {
        BsonFactory fac = new BsonFactory();
        fac.enable(BsonGenerator.Feature.ENABLE_STREAMING);
        return new ObjectMapper(fac);
    }
}
