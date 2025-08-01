package com.justin.eagle.bank;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    @Bean
    public Clock getSystemClock() {
        return Clock.systemUTC();

    }
}
