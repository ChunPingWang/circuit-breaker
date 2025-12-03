# Quickstart: Swagger API 文件

**Feature ID:** 2-swagger-api-docs
**Created:** 2025-12-03

## Prerequisites

- Java 17+
- Gradle 8.x
- 已完成 1-circuit-breaker-resilience 基礎建設

## 添加依賴

### 1. 更新根 build.gradle.kts

```kotlin
// 在 ext 或 buildscript 添加版本
val springDocVersion = "2.3.0"

// 在 subprojects 的 dependencies 添加
subprojects {
    dependencies {
        implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocVersion")
    }
}
```

### 2. 或各服務獨立配置

```kotlin
// mp-service/build.gradle.kts
dependencies {
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
}
```

## 配置 OpenAPI

### 1. 建立 OpenApiConfig

```java
// mp-service/src/main/java/.../config/OpenApiConfig.java
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
```

### 2. application.yml 設定

```yaml
# src/main/resources/application.yml
springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: method
    tags-sorter: alpha
    display-request-duration: true
```

## 添加 API 註解

### Controller 註解範例

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Process", description = "時間處理流程 API")
@RestController
@RequestMapping("/api")
public class ProcessController {

    @Operation(
        summary = "處理時間流程",
        description = "呼叫 gbp-service 取得當前時間，再傳遞至 gin-service。若 gin-service 無法連線，資料會暫存。"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功完成時間處理"),
        @ApiResponse(responseCode = "202", description = "服務降級 - 資料已暫存"),
        @ApiResponse(responseCode = "503", description = "服務不可用")
    })
    @GetMapping("/process")
    public ResponseEntity<ProcessResponse> process() {
        // ...
    }
}
```

### DTO 註解範例

```java
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "處理結果回應")
public class ProcessResponse {

    @Schema(description = "請求是否成功處理", example = "true")
    private boolean success;

    @Schema(description = "是否為降級模式", example = "false")
    private boolean degraded;

    @Schema(description = "從 gbp-service 取得的時間", example = "2025-12-03T10:30:00Z")
    private String timeData;

    @Schema(description = "處理結果訊息", example = "Time processed successfully")
    private String message;

    @Schema(description = "待傳送訊息數量（僅降級模式顯示）", example = "5", nullable = true)
    private Integer pendingCount;
}
```

## 存取 Swagger UI

啟動服務後，透過瀏覽器存取：

| Service | Swagger UI URL | API Docs URL |
|---------|----------------|--------------|
| mp-service | http://localhost:8080/swagger-ui.html | http://localhost:8080/v3/api-docs |
| gbp-service | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |
| gin-service | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |

## 測試 API

1. 開啟 Swagger UI（例如 http://localhost:8080/swagger-ui.html）
2. 展開想測試的端點（例如 GET /api/process）
3. 點擊 **Try it out** 按鈕
4. 填入必要參數（如有）
5. 點擊 **Execute** 按鈕
6. 查看回應狀態碼、內容與執行時間

## 下載 API 規格

### JSON 格式

```bash
curl http://localhost:8080/v3/api-docs
```

### YAML 格式

```bash
curl http://localhost:8080/v3/api-docs.yaml
```

## 生產環境設定

```yaml
# application-prod.yml
springdoc:
  swagger-ui:
    enabled: false
  api-docs:
    enabled: false
```

## 常見問題

### Swagger UI 無法載入

1. 確認依賴正確添加：`springdoc-openapi-starter-webmvc-ui`
2. 確認 Spring Boot 版本為 3.x
3. 檢查 `springdoc.swagger-ui.enabled` 是否為 `true`

### API 端點未顯示

1. 確認 Controller 有 `@RestController` 或 `@Controller` 註解
2. 確認 `@RequestMapping` 路徑正確
3. 檢查是否有 `@Hidden` 註解隱藏了端點

### 中文亂碼

在 application.yml 添加：

```yaml
spring:
  http:
    encoding:
      charset: UTF-8
      enabled: true
      force: true
```
