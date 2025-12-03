# Tasks: Swagger API 文件

**Feature ID:** 2-swagger-api-docs
**Spec:** .specify/features/2-swagger-api-docs/spec.md
**Plan:** .specify/features/2-swagger-api-docs/plan.md
**Generated:** 2025-12-03

## User Stories Mapping

| Story ID | Description | Priority | FRs |
|----------|-------------|----------|-----|
| US1 | API 使用者透過網頁介面瀏覽 API 規格 | P1 | FR-1, FR-4 |
| US2 | 開發人員直接在文件介面中測試 API | P1 | FR-2 |
| US3 | 系統整合者取得機器可讀 API 規格 | P2 | FR-3 |

## Phase 1: Setup (依賴配置)

### Objective
為三個微服務添加 SpringDoc OpenAPI 依賴

### Tasks

- [x] T001 Add SpringDoc version variable to root build.gradle.kts
- [x] T002 [P] Add springdoc-openapi-starter-webmvc-ui dependency to mp-service/build.gradle.kts
- [x] T003 [P] Add springdoc-openapi-starter-webmvc-ui dependency to gbp-service/build.gradle.kts
- [x] T004 [P] Add springdoc-openapi-starter-webmvc-ui dependency to gin-service/build.gradle.kts
- [x] T005 Run gradle build to verify no dependency conflicts

### Acceptance Criteria
- [x] All three services compile successfully with SpringDoc dependency
- [x] No version conflicts in dependency resolution

---

## Phase 2: Foundational (基礎配置)

### Objective
配置 SpringDoc 基本設定與 OpenAPI metadata

### Tasks

- [x] T006 [P] Add springdoc configuration to mp-service/src/main/resources/application.yml
- [x] T007 [P] Add springdoc configuration to gbp-service/src/main/resources/application.yml
- [x] T008 [P] Add springdoc configuration to gin-service/src/main/resources/application.yml
- [x] T009 [P] Create OpenApiConfig.java in mp-service/src/main/java/.../infrastructure/config/OpenApiConfig.java
- [x] T010 [P] Create OpenApiConfig.java in gbp-service/src/main/java/.../infrastructure/config/OpenApiConfig.java
- [x] T011 [P] Create OpenApiConfig.java in gin-service/src/main/java/.../infrastructure/config/OpenApiConfig.java

### Acceptance Criteria
- [x] Each service exposes /swagger-ui.html endpoint
- [x] Each service exposes /v3/api-docs endpoint
- [x] Service metadata (name, version, description) displayed correctly

---

## Phase 3: User Story 1 - API 文件瀏覽 (FR-1, FR-4)

### Story Goal
API 使用者可透過網頁介面瀏覽 API 規格，快速了解可用的端點與請求格式

### Independent Test Criteria
- [x] Swagger UI 顯示所有 API 端點
- [x] 每個端點顯示 HTTP 方法、路徑、描述
- [x] 服務資訊（名稱、版本、描述）正確顯示

### Tasks

#### mp-service API Annotations

- [x] T012 [P] [US1] Add @Tag annotation to ProcessController in mp-service/src/main/java/.../adapter/in/rest/ProcessController.java
- [x] T013 [P] [US1] Add @Operation and @ApiResponses to GET /api/process endpoint in mp-service
- [x] T014 [P] [US1] Add @Operation and @ApiResponses to GET /api/status endpoint in mp-service
- [x] T015 [P] [US1] Add @Schema annotations to ProcessResponse DTO in mp-service/src/main/java/.../adapter/in/rest/dto/ProcessResponse.java
- [x] T016 [P] [US1] Add @Schema annotations to StatusResponse DTO in mp-service/src/main/java/.../adapter/in/rest/dto/StatusResponse.java
- [x] T017 [P] [US1] Add @Schema annotations to ErrorResponse DTO in mp-service/src/main/java/.../adapter/in/rest/dto/ErrorResponse.java

#### gbp-service API Annotations

- [x] T018 [P] [US1] Add @Tag annotation to TimeController in gbp-service/src/main/java/.../adapter/in/rest/TimeController.java
- [x] T019 [P] [US1] Add @Operation and @ApiResponses to GET /api/time endpoint in gbp-service
- [x] T020 [P] [US1] Add @Schema annotations to TimeResponse DTO in gbp-service/src/main/java/.../adapter/in/rest/dto/TimeResponse.java

#### gin-service API Annotations

- [x] T021 [P] [US1] Add @Tag annotation to TimeController in gin-service/src/main/java/.../adapter/in/rest/TimeController.java
- [x] T022 [P] [US1] Add @Operation and @ApiResponses to POST /api/time endpoint in gin-service
- [x] T023 [P] [US1] Add @Schema annotations to TimeRequest DTO in gin-service/src/main/java/.../adapter/in/rest/dto/TimeRequest.java
- [x] T024 [P] [US1] Add @Schema annotations to AckResponse DTO in gin-service/src/main/java/.../adapter/in/rest/dto/AckResponse.java

---

## Phase 4: User Story 2 - 互動式測試 (FR-2)

### Story Goal
開發人員可直接在文件介面中測試 API，驗證 API 行為而無需切換工具

### Independent Test Criteria
- [ ] "Try it out" 按鈕可用
- [ ] 可輸入請求參數並執行 API 呼叫
- [ ] 回應狀態碼與內容正確顯示
- [ ] 錯誤回應有清楚的錯誤訊息

### Tasks

- [ ] T025 [US2] Configure CORS for Swagger UI in mp-service if needed (mp-service/src/main/java/.../infrastructure/config/CorsConfig.java)
- [ ] T026 [US2] Add example values to @Schema annotations for request parameters in mp-service DTOs
- [ ] T027 [US2] Add example values to @Schema annotations for response fields in all DTOs
- [ ] T028 [US2] Verify "Try it out" functionality works for all endpoints manually

---

## Phase 5: User Story 3 - API 規格匯出 (FR-3)

### Story Goal
系統整合者可取得 API 規格的機器可讀格式，自動產生客戶端程式碼

### Independent Test Criteria
- [ ] /v3/api-docs 回傳有效 JSON
- [ ] /v3/api-docs.yaml 回傳有效 YAML
- [ ] 規格包含所有端點的完整定義

### Tasks

- [ ] T029 [US3] Verify /v3/api-docs returns valid OpenAPI 3.0 JSON for mp-service
- [ ] T030 [US3] Verify /v3/api-docs returns valid OpenAPI 3.0 JSON for gbp-service
- [ ] T031 [US3] Verify /v3/api-docs returns valid OpenAPI 3.0 JSON for gin-service
- [ ] T032 [US3] Document API spec URLs in README or quickstart guide

---

## Phase 6: Integration Tests

### Objective
驗證文件功能正確運作

### Tasks

- [ ] T033 [P] Create SwaggerUiAccessTest.java in mp-service/src/test/java/.../integration/SwaggerUiAccessTest.java
- [ ] T034 [P] Create SwaggerUiAccessTest.java in gbp-service/src/test/java/.../integration/SwaggerUiAccessTest.java
- [ ] T035 [P] Create SwaggerUiAccessTest.java in gin-service/src/test/java/.../integration/SwaggerUiAccessTest.java
- [ ] T036 [P] Create OpenApiSpecTest.java in mp-service/src/test/java/.../integration/OpenApiSpecTest.java
- [ ] T037 [P] Create OpenApiSpecTest.java in gbp-service/src/test/java/.../integration/OpenApiSpecTest.java
- [ ] T038 [P] Create OpenApiSpecTest.java in gin-service/src/test/java/.../integration/OpenApiSpecTest.java
- [ ] T039 Run all integration tests and verify pass

### Test Specifications

**SwaggerUiAccessTest:**
- GET /swagger-ui.html returns 200 or redirect to swagger-ui/index.html
- Response contains expected HTML content

**OpenApiSpecTest:**
- GET /v3/api-docs returns 200
- Response is valid JSON
- JSON contains expected endpoints

---

## Phase 7: Polish & Production Config

### Objective
完善文件配置與生產環境設定

### Tasks

- [ ] T040 Create application-prod.yml with swagger disabled for mp-service
- [ ] T041 Create application-prod.yml with swagger disabled for gbp-service
- [ ] T042 Create application-prod.yml with swagger disabled for gin-service
- [ ] T043 Update README.md with Swagger UI URLs for each service
- [ ] T044 Final manual verification of all three Swagger UIs

---

## Dependencies

```
Phase 1 (Setup)
    │
    ▼
Phase 2 (Foundational)
    │
    ├───────────────────────────────┐
    ▼                               ▼
Phase 3 (US1: Browse)         Phase 4 (US2: Test)
    │                               │
    └───────────┬───────────────────┘
                ▼
          Phase 5 (US3: Export)
                │
                ▼
          Phase 6 (Integration Tests)
                │
                ▼
          Phase 7 (Polish)
```

## Parallel Execution Opportunities

### Phase 1
- T002, T003, T004 可平行執行（各服務獨立）

### Phase 2
- T006, T007, T008 可平行執行（application.yml）
- T009, T010, T011 可平行執行（OpenApiConfig）

### Phase 3
- T012-T017 (mp-service), T018-T020 (gbp-service), T021-T024 (gin-service) 可平行執行

### Phase 6
- T033-T038 可平行執行（各服務獨立測試）

## Implementation Strategy

### MVP Scope (Recommended)
Phase 1 + Phase 2 + Phase 3 (US1)

完成 MVP 後即可提供基本的 API 文件瀏覽功能，使用者可透過 Swagger UI 查看所有端點。

### Incremental Delivery

1. **Increment 1 (MVP):** Setup + Foundational + US1 → 基本文件瀏覽
2. **Increment 2:** US2 → 互動式測試功能
3. **Increment 3:** US3 + Tests + Polish → 規格匯出與生產配置

---

## Progress Summary

| Phase | Total | Done | In Progress |
|-------|-------|------|-------------|
| Phase 1: Setup | 5 | 5 | 0 |
| Phase 2: Foundational | 6 | 6 | 0 |
| Phase 3: US1 Browse | 13 | 13 | 0 |
| Phase 4: US2 Test | 4 | 0 | 0 |
| Phase 5: US3 Export | 4 | 0 | 0 |
| Phase 6: Tests | 7 | 0 | 0 |
| Phase 7: Polish | 5 | 0 | 0 |
| **TOTAL** | **44** | **24** | **0** |

---

## Notes

- All tasks follow P6 Framework Isolation: annotations only in infrastructure/presentation layers
- Swagger UI provides built-in "Try it out" functionality - no custom implementation needed
- Production environments should disable Swagger UI via configuration
- Tasks marked [P] can be executed in parallel with other [P] tasks in the same phase
