package com.circuitbreaker.mpservice.adapter.out.http;

import com.circuitbreaker.mpservice.domain.port.TimeReceiverPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Component
public class GinServiceClient implements TimeReceiverPort {

    private static final Logger log = LoggerFactory.getLogger(GinServiceClient.class);

    private final WebClient webClient;
    private final Duration timeout;

    public GinServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${services.gin-service.url:http://localhost:8082}") String baseUrl,
            @Value("${circuit-breaker.timeout-seconds:5}") int timeoutSeconds) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.timeout = Duration.ofSeconds(timeoutSeconds);
    }

    @Override
    public boolean sendTime(String timeData) {
        try {
            Map<String, String> request = Map.of(
                    "timeData", timeData,
                    "source", "mp-service"
            );

            webClient.post()
                    .uri("/api/time")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            Mono.error(new RuntimeException("gin-service returned error: " + clientResponse.statusCode())))
                    .toBodilessEntity()
                    .timeout(timeout)
                    .block();

            log.info("Successfully sent time to gin-service: {}", timeData);
            return true;

        } catch (Exception e) {
            log.error("Failed to send time to gin-service: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            // 使用簡單的連線測試
            Map<String, String> request = Map.of(
                    "timeData", "health-check",
                    "source", "mp-service-health-probe"
            );

            webClient.post()
                    .uri("/api/time")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
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
