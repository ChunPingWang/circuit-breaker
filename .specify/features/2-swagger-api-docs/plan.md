# Implementation Plan: Swagger API 文件

**Spec Reference:** .specify/features/2-swagger-api-docs/spec.md
**Created:** 2025-12-03
**Status:** Draft

## Technical Context

| Category | Decision |
|----------|----------|
| Language | Java 17 |
| Framework | Spring Boot 3 |
| Build Tool | Gradle |
| API Documentation | SpringDoc OpenAPI (Swagger UI) |
| OpenAPI Version | 3.0 |

## Constitution Compliance Checklist

Before implementation, verify alignment with project principles:

- [x] **P1 - Code Quality:** Swagger 註解遵循一致的格式規範
- [x] **P2 - Testing:** 整合測試驗證文件介面可載入與規格正確
- [x] **P3 - BDD:** 場景涵蓋瀏覽、測試、匯出功能
- [x] **P4 - DDD:** 文件功能屬於基礎設施層，不影響領域層
- [x] **P5 - SOLID:** 文件配置透過 Spring Configuration 獨立管理
- [x] **P6 - Framework Isolation:** 僅在 Controller 層添加註解，領域層不受影響
- [x] **P7 - UX Consistency:** 使用標準 Swagger UI，符合業界慣例
- [x] **P8 - Performance:** Swagger UI 靜態資源，載入時間 < 2 秒

## Overview

為三個微服務整合 SpringDoc OpenAPI，提供 Swagger UI 互動式文件介面。每個服務獨立提供 `/swagger-ui.html` 與 `/v3/api-docs` 端點，使用者可透過統一的介面瀏覽、測試 API。

## Architecture Decisions

### SpringDoc OpenAPI vs Springfox

選擇 **SpringDoc OpenAPI** 而非 Springfox：
- SpringDoc 支援 Spring Boot 3 與 Jakarta EE
- Springfox 不再積極維護，不支援 Spring Boot 3
- SpringDoc 與 OpenAPI 3.0 標準完全相容

### 文件生成策略

採用 **註解驅動** 方式生成文件：
- 在 Controller 類別與方法上添加 OpenAPI 註解
- 自動掃描並生成 API 規格
- 與現有 openapi.yaml 契約保持一致

### Layer Responsibilities

| Layer | Responsibilities |
|-------|-----------------|
| Infrastructure | SpringDoc 配置、Swagger UI、OpenAPI 註解 |
| Presentation | Controller 類別添加 @Operation、@ApiResponse 等註解 |
| Domain | 無影響（P6 框架隔離） |

## Implementation Phases

### Phase 1: 依賴配置

**Objective:** 為三個服務添加 SpringDoc 依賴

**Tasks:**
1. 在根 build.gradle.kts 添加 SpringDoc 依賴版本管理
2. 在各服務 build.gradle.kts 添加 springdoc-openapi-starter-webmvc-ui
3. 驗證依賴正確引入

**Acceptance Criteria:**
- [ ] Gradle 依賴正確配置
- [ ] 無版本衝突

### Phase 2: 基礎配置

**Objective:** 配置 SpringDoc 基本設定

**Tasks:**
1. 為每個服務建立 OpenApiConfig 配置類別
2. 設定服務名稱、版本、描述
3. 設定 Swagger UI 路徑
4. 配置 CORS（如需要）

**Acceptance Criteria:**
- [ ] 每個服務可存取 /swagger-ui.html
- [ ] API 規格可存取 /v3/api-docs

### Phase 3: API 註解

**Objective:** 為所有 API 端點添加文件註解

**Tasks:**
1. mp-service: 為 ProcessController、StatusController 添加註解
2. gbp-service: 為 TimeController 添加註解
3. gin-service: 為 TimeController 添加註解
4. 為所有 DTO 類別添加 @Schema 註解

**Acceptance Criteria:**
- [ ] 所有端點顯示於 Swagger UI
- [ ] 每個端點有清楚的描述與範例
- [ ] 請求/回應格式有完整說明

### Phase 4: 整合測試

**Objective:** 驗證文件功能正確運作

**Tasks:**
1. 撰寫測試驗證 /swagger-ui.html 可載入
2. 撰寫測試驗證 /v3/api-docs 回傳正確 JSON
3. 驗證測試呼叫功能正常

**Acceptance Criteria:**
- [ ] Swagger UI 載入時間 < 2 秒
- [ ] API 規格格式正確
- [ ] 測試呼叫功能正常

## Testing Strategy

### Integration Tests

- SwaggerUiAccessTest: 驗證 /swagger-ui.html 回傳 200
- OpenApiSpecTest: 驗證 /v3/api-docs 回傳有效 JSON
- ApiDocumentationConsistencyTest: 驗證文件與實際 API 一致

## Configuration

### application.yml 設定

```yaml
# SpringDoc OpenAPI Configuration
springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: method
    tags-sorter: alpha
```

### OpenApiConfig.java 範例

```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("MP Service API")
                .version("1.0.0")
                .description("主服務 API - 協調 gbp-service 與 gin-service")
                .contact(new Contact()
                    .name("Circuit Breaker Team")));
    }
}
```

## Service Endpoints

| Service | Swagger UI | API Docs |
|---------|------------|----------|
| mp-service | http://localhost:8080/swagger-ui.html | http://localhost:8080/v3/api-docs |
| gbp-service | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |
| gin-service | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |

## Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| springdoc-openapi-starter-webmvc-ui | 2.3.0 | Swagger UI 與 OpenAPI 生成 |

## Risk Assessment

| Risk | Impact | Mitigation |
|------|--------|------------|
| SpringDoc 版本與 Spring Boot 3 不相容 | H | 使用官方推薦的 starter 版本 |
| 文件與實際 API 不同步 | M | 整合測試自動驗證一致性 |
| Swagger UI 效能影響 | L | 生產環境可透過設定停用 |

## Rollback Plan

1. **停用 Swagger UI**：設定 `springdoc.swagger-ui.enabled=false`
2. **移除依賴**：從 build.gradle.kts 移除 springdoc 依賴
3. **保留 API 契約**：手動維護的 openapi.yaml 仍可使用
