package com.circuitbreaker.mpservice.infrastructure.config;

import com.circuitbreaker.mpservice.application.service.ProcessTimeService;
import com.circuitbreaker.mpservice.domain.port.MessageRepository;
import com.circuitbreaker.mpservice.domain.port.TimeProviderPort;
import com.circuitbreaker.mpservice.domain.port.TimeReceiverPort;
import com.circuitbreaker.mpservice.domain.service.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CircuitBreakerConfig {

    @Value("${circuit-breaker.failure-threshold:3}")
    private int failureThreshold;

    @Value("${circuit-breaker.success-threshold:2}")
    private int successThreshold;

    @Value("${circuit-breaker.retry-interval-seconds:5}")
    private int retryIntervalSeconds;

    @Bean
    public CircuitBreaker circuitBreaker() {
        return new CircuitBreaker(failureThreshold, successThreshold, retryIntervalSeconds);
    }

    @Bean
    public ProcessTimeService processTimeService(
            TimeProviderPort timeProvider,
            TimeReceiverPort timeReceiver,
            MessageRepository messageRepository,
            CircuitBreaker circuitBreaker) {
        return new ProcessTimeService(timeProvider, timeReceiver, messageRepository, circuitBreaker);
    }
}
