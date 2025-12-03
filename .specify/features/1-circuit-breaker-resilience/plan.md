# Implementation Plan: 微服務斷路器韌性機制

**Spec Reference:** .specify/features/1-circuit-breaker-resilience/spec.md
**Created:** 2025-12-03
**Status:** Draft

## Technical Context

| Category | Decision |
|----------|----------|
| Language | Java 17 |
| Framework | Spring Boot 3 |
| Build Tool | Gradle |
| Testing | JUnit 5, Cucumber (BDD) |
| Architecture | Hexagonal (Ports & Adapters) |
| Database | H2 (embedded) |
| HTTP Client | Spring WebClient (non-blocking) |

## Constitution Compliance Checklist

Before implementation, verify alignment with project principles:

- [x] **P1 - Code Quality:** Quality gates defined - Checkstyle, SpotBugs, cyclomatic complexity < 10
- [x] **P2 - Testing:** Test strategy covers unit (80%+), integration, acceptance (Cucumber)
- [x] **P3 - BDD:** Given-When-Then scenarios defined in spec.md, to be implemented with Cucumber
- [x] **P4 - DDD:** Domain boundaries identified - CircuitBreaker, MessageQueue aggregates
- [x] **P5 - SOLID:** Design adheres to SOLID - interfaces for all dependencies
- [x] **P6 - Framework Isolation:** Domain layer has no Spring imports, infrastructure layer handles framework
- [x] **P7 - UX Consistency:** API responses follow consistent JSON structure with clear error messages
- [x] **P8 - Performance:** Benchmarks defined - 500ms normal, 200ms degraded, 100 RPS

## Overview

實作三個獨立的 Spring Boot 微服務，採用六角形架構確保領域邏輯與框架隔離。mp-service 作為主要入口，實作斷路器模式處理 gin-service 的連線失敗，並透過 H2 資料庫暫存未傳遞的資料。

## Architecture Decisions

### Domain Model Changes

**Entities:**
- `PendingMessage` - 待傳遞訊息，包含 id, timestamp, createdAt, status

**Value Objects:**
- `MessageStatus` - 列舉：PENDING, SENT, FAILED
- `CircuitState` - 列舉：CLOSED, OPEN, HALF_OPEN

**Aggregates:**
- `CircuitBreaker` - 管理斷路器狀態轉換邏輯
- `MessageQueue` - 管理 PendingMessage 的生命週期

### Bounded Context Impact

三個獨立的限界上下文：

| Service | Bounded Context | Responsibility |
|---------|-----------------|----------------|
| mp-service | Orchestration | 協調服務呼叫、斷路器、訊息暫存 |
| gbp-service | TimeProvider | 提供當前時間 |
| gin-service | TimeReceiver | 接收並處理時間資料 |

### Layer Responsibilities (Hexagonal Architecture)

| Layer | Responsibilities |
|-------|-----------------|
| Domain | CircuitBreaker, MessageQueue, PendingMessage, Domain Events |
| Application (Ports) | UseCase interfaces, Input/Output ports |
| Infrastructure (Adapters) | Spring Controllers, JPA Repositories, HTTP Clients, Schedulers |

### Service Port Allocation

| Service | Port | Description |
|---------|------|-------------|
| mp-service | 8080 | 主服務入口 |
| gbp-service | 8081 | 時間查詢服務 |
| gin-service | 8082 | 時間接收服務 |

## Implementation Phases

### Phase 1: 基礎服務建立

**Objective:** 建立三個服務的基礎架構與正常流程

**Tasks:**
1. 建立 Gradle multi-module 專案結構
2. 實作 gbp-service - GET /api/time 端點
3. 實作 gin-service - POST /api/time 端點
4. 實作 mp-service 基礎 - GET /api/process 端點
5. 實作服務間 HTTP 呼叫（無斷路器）

**Acceptance Criteria:**
- [ ] 三服務可獨立啟動
- [ ] mp-service 成功串接 gbp-service 和 gin-service
- [ ] gbp-service 在 console 輸出時間日誌

### Phase 2: 領域層實作

**Objective:** 實作核心領域邏輯（框架無關）

**Tasks:**
1. 實作 CircuitBreaker 領域物件（純 Java，無 Spring）
2. 實作 CircuitState 狀態機轉換
3. 實作 PendingMessage 實體與 MessageStatus
4. 實作 MessageQueue 聚合根
5. 定義領域事件介面

**Acceptance Criteria:**
- [ ] 領域層無任何框架依賴
- [ ] 單元測試覆蓋率 100% 對關鍵路徑
- [ ] 斷路器狀態轉換符合規格

### Phase 3: 應用層實作

**Objective:** 實作 Use Cases 與 Port 介面

**Tasks:**
1. 定義 ProcessTimeUseCase（輸入埠）
2. 定義 TimeProviderPort, TimeReceiverPort（輸出埠）
3. 定義 MessageRepository（輸出埠）
4. 實作 ProcessTimeService（協調斷路器與訊息暫存）

**Acceptance Criteria:**
- [ ] 應用層僅依賴領域層與介面
- [ ] Use Case 完整處理正常與異常流程

### Phase 4: 基礎設施層實作

**Objective:** 實作 Adapters（框架整合）

**Tasks:**
1. 實作 Spring WebClient adapter（TimeProviderPort, TimeReceiverPort）
2. 實作 JPA Repository adapter（MessageRepository）
3. 實作 Spring Scheduler adapter（健康探測、補發排程）
4. 實作 REST Controller adapter（ProcessTimeUseCase）
5. 配置 H2 資料庫

**Acceptance Criteria:**
- [ ] 所有 Adapters 實作對應 Port 介面
- [ ] 資料庫 schema 自動建立
- [ ] 排程任務正確執行

### Phase 5: 整合測試與 BDD

**Objective:** 驗證完整功能與 BDD 場景

**Tasks:**
1. 實作 Cucumber 測試 feature files
2. 實作整合測試（三服務協調）
3. 實作斷線情境測試
4. 實作效能基準測試
5. 配置 CI pipeline

**Acceptance Criteria:**
- [ ] 所有 BDD 場景通過
- [ ] 整合測試覆蓋所有關鍵流程
- [ ] 效能符合規格要求

## Testing Strategy

### Unit Tests
- CircuitBreaker: 狀態轉換邏輯、失敗計數、成功重置
- MessageQueue: 訊息新增、狀態更新、依序取出
- ProcessTimeService: 正常流程、斷線處理、補發邏輯

### Integration Tests
- mp-service ↔ gbp-service: HTTP 呼叫與回應解析
- mp-service ↔ gin-service: HTTP 呼叫與錯誤處理
- mp-service ↔ H2: 資料暫存與查詢

### Acceptance Tests (BDD)
- Scenario: 正常流程 - 所有服務可用
  - Given: gbp-service 和 gin-service 正常運作
  - When: 使用者呼叫 mp-service API
  - Then: 成功回應，gin-service 收到時間資料

- Scenario: gin-service 斷線時的資料暫存
  - Given: gin-service 無法連線
  - When: 使用者呼叫 mp-service API
  - Then: 資料暫存至資料庫，使用者收到降級通知

- Scenario: gin-service 恢復後的資料補發
  - Given: 資料庫有待傳遞資料，gin-service 恢復
  - When: 健康探測成功
  - Then: 暫存資料依序傳送

### Performance Tests
- 正常流程 API 回應時間 < 500ms (p95)
- 斷線情況 API 回應時間 < 200ms (p95)
- 補發 100 筆資料 < 30 秒
- 支援 100 RPS 並發請求

## Risk Assessment

| Risk | Impact | Mitigation |
|------|--------|------------|
| 斷路器狀態不一致 | H | 使用 synchronized 或 AtomicReference 確保執行緒安全 |
| H2 資料庫容量限制 | M | 設定最大資料筆數，超過時告警 |
| 排程任務重複執行 | M | 使用 @Scheduled 的 fixedDelay 確保序列執行 |
| WebClient 連線池耗盡 | M | 配置適當的 connection pool 大小與 timeout |

## Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 3.x | 框架基礎 |
| Spring WebFlux | 3.x | WebClient HTTP 用戶端 |
| Spring Data JPA | 3.x | 資料庫存取 |
| H2 Database | 2.x | 嵌入式資料庫 |
| Cucumber | 7.x | BDD 測試框架 |
| JUnit 5 | 5.x | 單元測試框架 |

## Rollback Plan

1. **服務層級回滾**：各服務獨立部署，可單獨回滾
2. **資料庫回滾**：H2 為嵌入式，重啟即清空（開發環境）
3. **功能開關**：可透過設定檔停用斷路器（直接呼叫模式）

## Configuration

### Circuit Breaker Settings (application.yml)

```yaml
circuit-breaker:
  failure-threshold: 3          # 連續失敗次數觸發開啟
  success-threshold: 2          # 連續成功次數觸發關閉
  timeout-seconds: 5            # 連線逾時秒數
  retry-interval-seconds: 5     # 重試間隔（預設）
  retry-duration-seconds: 60    # 重試持續時間（預設）
```

### Service URLs (application.yml)

```yaml
services:
  gbp-service:
    url: http://localhost:8081
  gin-service:
    url: http://localhost:8082
```
