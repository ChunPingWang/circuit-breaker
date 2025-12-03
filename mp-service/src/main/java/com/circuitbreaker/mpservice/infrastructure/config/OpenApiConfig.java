package com.circuitbreaker.mpservice.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("MP Service API")
                .version("1.0.0")
                .description("主服務 API - 協調 gbp-service 與 gin-service，實作斷路器韌性機制")
                .contact(new Contact()
                    .name("Circuit Breaker Team")));
    }
}
