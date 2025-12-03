package com.circuitbreaker.ginservice.infrastructure.config;

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
                .title("GIN Service API")
                .version("1.0.0")
                .description("時間接收服務 - 接收並儲存 mp-service 傳遞的時間資料")
                .contact(new Contact()
                    .name("Circuit Breaker Team")));
    }
}
