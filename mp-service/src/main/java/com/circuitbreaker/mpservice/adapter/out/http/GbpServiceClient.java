package com.circuitbreaker.mpservice.adapter.out.http;

import com.circuitbreaker.mpservice.domain.port.TimeProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Component
public class GbpServiceClient implements TimeProviderPort {

    private static final Logger log = LoggerFactory.getLogger(GbpServiceClient.class);

    private final WebClient webClient;
    private final Duration timeout;

    public GbpServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${services.gbp-service.url:http://localhost:8081}") String baseUrl,
            @Value("${circuit-breaker.timeout-seconds:5}") int timeoutSeconds) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.timeout = Duration.ofSeconds(timeoutSeconds);
    }

    @Override
    public Optional<String> getCurrentTime() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.get()
                    .uri("/api/time")
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            Mono.error(new RuntimeException("gbp-service returned error: " + clientResponse.statusCode())))
                    .bodyToMono(Map.class)
                    .timeout(timeout)
                    .block();

            if (response != null && response.containsKey("currentTime")) {
                String currentTime = (String) response.get("currentTime");
                log.info("Received time from gbp-service: {}", currentTime);
                return Optional.of(currentTime);
            }

            log.warn("Invalid response from gbp-service: {}", response);
            return Optional.empty();

        } catch (Exception e) {
            log.error("Failed to get time from gbp-service: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            webClient.get()
                    .uri("/api/time")
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(2))
                    .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
