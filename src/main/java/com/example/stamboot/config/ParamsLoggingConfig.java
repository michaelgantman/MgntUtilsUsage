package com.example.stamboot.config;

import com.mgnt.lifecycle.management.backgroundrunner.BackgroundThreadsRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;

@Configuration
public class ParamsLoggingConfig {

    @Bean
    @DependsOn("paramLoggingTask")
    @Lazy(false)
    public BackgroundThreadsRunner backgroundThreadsRunner() {
        return new BackgroundThreadsRunner(1);
    }
}
