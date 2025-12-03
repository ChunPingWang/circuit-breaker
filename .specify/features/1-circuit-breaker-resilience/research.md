# Research: 微服務斷路器韌性機制

**Feature ID:** 1-circuit-breaker-resilience
**Created:** 2025-12-03

## Technology Decisions

### 1. Circuit Breaker Implementation

**Decision:** 自行實作領域層斷路器，不使用 Resilience4j

**Rationale:**
- 符合 P6 框架隔離原則：領域邏輯不依賴外部函式庫
- 斷路器邏輯相對簡單，自行實作可完全控制行為
- 避免 Resilience4j 的 Spring Boot 整合污染領域層

**Alternatives Considered:**
- Resilience4j：功能完整但與 Spring 耦合，違反框架隔離
- Hystrix：已停止維護
- Spring Cloud Circuit Breaker：抽象層過重

### 2. HTTP Client

**Decision:** Spring WebClient (WebFlux)

**Rationale:**
- Spring Boot 3 官方推薦的非阻塞 HTTP 用戶端
- 支援響應式程式設計，更好的資源利用
- 內建 timeout 與 retry 機制易於配置

**Alternatives Considered:**
- RestTemplate：已標記為維護模式
- Apache HttpClient：需額外整合，不如 WebClient 簡潔
- OkHttp：功能足夠但非 Spring 生態系首選

### 3. Database

**Decision:** H2 Embedded Database

**Rationale:**
- 符合 Tech.md 指定的技術選擇
- 開發測試方便，無需額外安裝
- 支援 JPA 標準介面，未來可輕鬆切換至其他資料庫

**Alternatives Considered:**
- PostgreSQL：生產環境首選，但開發環境設定複雜
- SQLite：不支援 JPA 標準
- 記憶體 Map：不具持久性，重啟遺失資料

### 4. BDD Testing Framework

**Decision:** Cucumber-JVM with JUnit 5

**Rationale:**
- 符合 P3 BDD 原則
- 成熟穩定的 Java BDD 框架
- 與 JUnit 5 整合良好

**Alternatives Considered:**
- Spock：Groovy 語法學習曲線
- JBehave：社群活躍度較低
- Karate：偏向 API 測試，不適合 BDD 場景

### 5. Scheduling

**Decision:** Spring @Scheduled with fixedDelay

**Rationale:**
- Spring Boot 內建，無需額外依賴
- fixedDelay 確保序列執行，避免重複觸發
- 簡單配置，符合單實例運作假設

**Alternatives Considered:**
- Quartz：功能過重，適合分散式排程
- ScheduledExecutorService：需自行管理生命週期

### 6. Project Structure

**Decision:** Gradle Multi-Module Project

**Rationale:**
- 符合 Tech.md 指定的建構工具
- 各服務獨立模組，共享 domain 模組
- 統一依賴版本管理

**Structure:**
```
circuit-breaker/
├── buildSrc/                    # Gradle conventions
├── shared/
│   └── domain/                  # 共享領域模組
├── mp-service/                  # 主服務
├── gbp-service/                 # 時間查詢服務
├── gin-service/                 # 時間接收服務
└── settings.gradle.kts
```

## Best Practices Applied

### Hexagonal Architecture Patterns

1. **Input Ports (Driving Adapters)**
   - REST Controllers 呼叫 Use Case interfaces
   - Scheduler 呼叫 Replay Use Case

2. **Output Ports (Driven Adapters)**
   - Repository interfaces 定義於 domain/application
   - HTTP Client interfaces 定義於 application
   - 實作位於 infrastructure

3. **Dependency Rule**
   - Domain ← Application ← Infrastructure
   - 依賴方向由外向內
   - 內層不知道外層存在

### Circuit Breaker State Machine

```
     ┌─────────────────────────────────────┐
     │                                     │
     ▼                                     │
  ┌──────┐   3 failures    ┌──────┐       │
  │CLOSED│ ──────────────► │ OPEN │       │
  └──────┘                 └──────┘       │
     ▲                         │          │
     │                         │ timer    │
     │ 2 successes             ▼          │
     │                    ┌─────────┐     │
     └────────────────────│HALF_OPEN│─────┘
                          └─────────┘  failure
```

### Error Handling Strategy

1. **Timeout Handling**
   - WebClient 設定 5 秒 responseTimeout
   - 逾時視為連線失敗

2. **Connection Error**
   - IOException 類別錯誤視為連線失敗
   - 4xx/5xx HTTP 狀態碼視為連線失敗

3. **Graceful Degradation**
   - 斷線時回傳 HTTP 202 Accepted
   - 回應包含 degraded: true 標記
   - 使用者收到明確的狀態說明

## Resolved Clarifications

所有技術相關疑問已透過 Tech.md 與上述決策解決，無待釐清項目。
