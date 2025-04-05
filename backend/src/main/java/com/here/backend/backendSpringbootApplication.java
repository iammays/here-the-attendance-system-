package com.here.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class backendSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(backendSpringbootApplication.class, args);
    }

    // تعليق: تعريف Bean لـ RestTemplate
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}