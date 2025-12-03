package com.circuitbreaker.gbpservice.infrastructure.config;

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
                .title("GBP Service API")
                .version("1.0.0")
                .description("時間提供服務 - 提供當前時間給 mp-service")
                .contact(new Contact()
                    .name("Circuit Breaker Team")));
    }
}
