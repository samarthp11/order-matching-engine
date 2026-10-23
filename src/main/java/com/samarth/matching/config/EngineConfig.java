package com.samarth.matching.config;

import com.samarth.matching.engine.MatchingEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EngineConfig {

    @Bean
    public MatchingEngine matchingEngine() {
        return new MatchingEngine(false);
    }
}