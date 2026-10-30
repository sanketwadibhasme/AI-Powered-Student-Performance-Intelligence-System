package com.sanket.studentperformanceintelligencesystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * RestClient used to call the Python AI/ML inference service (Flask).
 * The service URL is configurable via the 'ai.service.url' property.
 */
@Configuration
public class RestClientConfig {

    @Value("${ai.service.url:http://127.0.0.1:5001}")
    private String aiServiceUrl;

    @Bean
    public RestClient aiRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        // Read timeout must cover a legitimate first model inference (pandas/
        // sklearn warm-up) yet stay well below the frontend's 60s AI timeout.
        factory.setReadTimeout(Duration.ofSeconds(30));

        return RestClient.builder()
                .baseUrl(aiServiceUrl)
                .requestFactory(factory)
                .build();
    }
}
