# Circuit Breaker Microservices

具備斷路器韌性機制的微服務架構範例專案。

## 目錄

- [專案概述](#專案概述)
- [系統架構](#系統架構)
- [服務說明](#服務說明)
- [循序圖](#循序圖)
- [資料庫結構](#資料庫結構)
- [環境需求](#環境需求)
- [快速開始](#快速開始)
- [設定說明](#設定說明)
- [API 文件](#api-文件)
- [測試方法](#測試方法)
- [專案結構](#專案結構)

## 專案概述

本專案實作一個具備韌性的微服務架構，包含三個服務：

- **mp-service**：主服務，協調下游服務並實作斷路器模式
- **gbp-service**：時間提供服務，回傳當前系統時間
- **gin-service**：時間接收服務，接收並儲存時間資料

當 gin-service 無法連線時，mp-service 會將資料暫存至資料庫，待服務恢復後自動補發。

### 主要特色

- 斷路器模式（Circuit Breaker Pattern）
- 服務降級與資料暫存
- 自動恢復與資料補發
- Swagger API 文件
- 六角形架構（Hexagonal Architecture）

## 系統架構

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           使用者請求                                      │
└─────────────────────────────────┬───────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         mp-service (Port 8080)                           │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                      Presentation Layer                          │   │
│  │  ┌─────────────────┐  ┌─────────────────┐                       │   │
│  │  │ ProcessController│  │   Swagger UI    │                       │   │
│  │  └────────┬────────┘  └─────────────────┘                       │   │
│  └───────────┼──────────────────────────────────────────────────────┘   │
│              │                                                           │
│  ┌───────────┼──────────────────────────────────────────────────────┐   │
│  │           ▼            Application Layer                          │   │
│  │  ┌─────────────────┐                                              │   │
│  │  │  ProcessService │                                              │   │
│  │  └────────┬────────┘                                              │   │
│  └───────────┼──────────────────────────────────────────────────────┘   │
│              │                                                           │
│  ┌───────────┼──────────────────────────────────────────────────────┐   │
│  │           ▼              Domain Layer                             │   │
│  │  ┌─────────────────┐  ┌─────────────────┐                        │   │
│  │  │ CircuitBreaker  │  │  MessageQueue   │                        │   │
│  │  │ (State Machine) │  │   (Aggregate)   │                        │   │
│  │  └─────────────────┘  └─────────────────┘                        │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│              │                                                           │
│  ┌───────────┼──────────────────────────────────────────────────────┐   │
│  │           ▼          Infrastructure Layer                         │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐   │   │
│  │  │ GbpClient    │  │ GinClient    │  │ PendingMessageRepo   │   │   │
│  │  │ (WebClient)  │  │ (WebClient)  │  │ (JPA + H2)           │   │   │
│  │  └──────┬───────┘  └──────┬───────┘  └──────────────────────┘   │   │
│  └─────────┼─────────────────┼──────────────────────────────────────┘   │
└────────────┼─────────────────┼──────────────────────────────────────────┘
             │                 │
             ▼                 ▼
┌────────────────────┐  ┌────────────────────┐
│  gbp-service       │  │  gin-service       │
│  (Port 8081)       │  │  (Port 8082)       │
│                    │  │                    │
│  GET /api/time     │  │  POST /api/time    │
│  回傳當前時間       │  │  接收時間資料       │
└────────────────────┘  └────────────────────┘
```

## 服務說明

### mp-service (主服務)

| 項目 | 說明 |
|------|------|
| Port | 8080 |
| 功能 | 協調 gbp-service 與 gin-service，實作斷路器機制 |
| 資料庫 | H2 (記憶體模式) |

**API 端點：**

| Method | Path | 說明 |
|--------|------|------|
| GET | `/api/process` | 執行時間處理流程 |
| GET | `/api/status` | 查詢系統狀態 |

### gbp-service (時間提供服務)

| 項目 | 說明 |
|------|------|
| Port | 8081 |
| 功能 | 提供當前系統時間 |

**API 端點：**

| Method | Path | 說明 |
|--------|------|------|
| GET | `/api/time` | 取得當前時間 |

### gin-service (時間接收服務)

| 項目 | 說明 |
|------|------|
| Port | 8082 |
| 功能 | 接收並儲存時間資料 |

**API 端點：**

| Method | Path | 說明 |
|--------|------|------|
| POST | `/api/time` | 接收時間資料 |

## 循序圖

### 正常流程

```
┌──────┐     ┌────────────┐     ┌────────────┐     ┌────────────┐
│Client│     │ mp-service │     │gbp-service │     │gin-service │
└──┬───┘     └─────┬──────┘     └─────┬──────┘     └─────┬──────┘
   │               │                  │                  │
   │ GET /process  │                  │                  │
   │──────────────>│                  │                  │
   │               │                  │                  │
   │               │  GET /api/time   │                  │
   │               │─────────────────>│                  │
   │               │                  │                  │
   │               │  TimeResponse    │                  │
   │               │<─────────────────│                  │
   │               │                  │                  │
   │               │                POST /api/time       │
   │               │────────────────────────────────────>│
   │               │                  │                  │
   │               │                AckResponse          │
   │               │<────────────────────────────────────│
   │               │                  │                  │
   │ ProcessResponse (success=true, degraded=false)      │
   │<──────────────│                  │                  │
   │               │                  │                  │
```

### gin-service 斷線流程

```
┌──────┐     ┌────────────┐     ┌────────────┐     ┌────────────┐
│Client│     │ mp-service │     │gbp-service │     │gin-service │
└──┬───┘     └─────┬──────┘     └─────┬──────┘     └─────┬──────┘
   │               │                  │                  │
   │ GET /process  │                  │                  │
   │──────────────>│                  │                  │
   │               │                  │                  │
   │               │  GET /api/time   │                  │
   │               │─────────────────>│                  │
   │               │                  │                  │
   │               │  TimeResponse    │                  │
   │               │<─────────────────│                  │
   │               │                  │                  │
   │               │                POST /api/time       │
   │               │─────────────────────────────────X   │ (連線失敗)
   │               │                  │                  │
   │               │ ┌──────────────────────────────┐    │
   │               │ │ 斷路器: 記錄失敗              │    │
   │               │ │ 暫存資料至 H2 資料庫         │    │
   │               │ └──────────────────────────────┘    │
   │               │                  │                  │
   │ ProcessResponse (success=true, degraded=true)       │
   │<──────────────│                  │                  │
   │               │                  │                  │
```

### 斷路器狀態轉換

```
                    連續 3 次失敗
        ┌─────────────────────────────────┐
        │                                 │
        ▼                                 │
    ┌───────┐                         ┌───┴───┐
    │ OPEN  │───── 重試間隔到達 ─────>│CLOSED │
    └───┬───┘                         └───────┘
        │                                 ▲
        │ 重試間隔到達                     │
        ▼                                 │
    ┌─────────┐     連續 2 次成功         │
    │HALF_OPEN│───────────────────────────┘
    └────┬────┘
         │
         │ 探測失敗
         ▼
    ┌────────┐
    │  OPEN  │
    └────────┘
```

### 資料補發流程

```
┌────────────┐     ┌────────────┐     ┌──────────────────┐
│ mp-service │     │gin-service │     │ pending_messages │
└─────┬──────┘     └─────┬──────┘     └────────┬─────────┘
      │                  │                     │
      │ 斷路器轉為 CLOSED │                     │
      │ (服務恢復)        │                     │
      │                  │                     │
      │ 查詢待傳送訊息    │                     │
      │────────────────────────────────────────>│
      │                  │                     │
      │ 返回 PENDING 訊息清單                   │
      │<────────────────────────────────────────│
      │                  │                     │
      │ 逐筆傳送         │                     │
      ├─────────────────>│                     │
      │                  │                     │
      │ AckResponse      │                     │
      │<─────────────────│                     │
      │                  │                     │
      │ 更新狀態為 SENT  │                     │
      │────────────────────────────────────────>│
      │                  │                     │
      │ (重複直到全部完成或失敗)                 │
      │                  │                     │
```

## 資料庫結構

### Entity: PendingMessage

mp-service 使用 H2 資料庫暫存待傳送訊息。

```sql
CREATE TABLE pending_messages (
    id UUID PRIMARY KEY,
    time_data VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP NULL,

    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'SENT', 'FAILED'))
);

CREATE INDEX idx_pending_messages_status ON pending_messages(status);
CREATE INDEX idx_pending_messages_created_at ON pending_messages(created_at);
```

### ER Diagram

```
┌─────────────────────────────────────────────┐
│              pending_messages                │
├─────────────────────────────────────────────┤
│ id          │ UUID         │ PK             │
│ time_data   │ VARCHAR(255) │ NOT NULL       │
│ status      │ VARCHAR(20)  │ NOT NULL       │
│ created_at  │ TIMESTAMP    │ NOT NULL       │
│ sent_at     │ TIMESTAMP    │ NULLABLE       │
└─────────────────────────────────────────────┘
```

### MessageStatus 列舉

| 狀態 | 說明 |
|------|------|
| PENDING | 等待傳送 |
| SENT | 已成功傳送 |
| FAILED | 傳送失敗（保留未來使用） |

## 環境需求

- **Java**: 17+
- **Gradle**: 8.x
- **IDE**: IntelliJ IDEA / VS Code (建議)

## 快速開始

### 1. Clone 專案

```bash
git clone <repository-url>
cd circuit-breaker
```

### 2. 建置專案

```bash
./gradlew build
```

### 3. 啟動服務

開啟三個終端機，分別啟動三個服務：

**終端機 1 - gbp-service (Port 8081)**
```bash
./gradlew :gbp-service:bootRun
```

**終端機 2 - gin-service (Port 8082)**
```bash
./gradlew :gin-service:bootRun
```

**終端機 3 - mp-service (Port 8080)**
```bash
./gradlew :mp-service:bootRun
```

### 4. 測試 API

```bash
# 執行時間處理流程
curl http://localhost:8080/api/process

# 查詢系統狀態
curl http://localhost:8080/api/status

# 取得當前時間 (gbp-service)
curl http://localhost:8081/api/time

# 傳送時間資料 (gin-service)
curl -X POST http://localhost:8082/api/time \
  -H "Content-Type: application/json" \
  -d '{"timeData": "2025-12-03T10:30:00Z", "source": "test"}'
```

## 設定說明

### mp-service/application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: mp-service
  datasource:
    url: jdbc:h2:mem:mpdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true          # 啟用 H2 Console
      path: /h2-console      # H2 Console 路徑
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

# SpringDoc OpenAPI 配置
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

# 斷路器設定 (待實作)
circuit-breaker:
  failure-threshold: 3       # 連續失敗次數門檻
  success-threshold: 2       # 連續成功次數門檻（半開狀態）
  timeout-seconds: 5         # 呼叫逾時秒數
  retry-interval-seconds: 10 # 重試間隔秒數
```

### gbp-service/application.yml

```yaml
server:
  port: 8081

spring:
  application:
    name: gbp-service

springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
```

### gin-service/application.yml

```yaml
server:
  port: 8082

spring:
  application:
    name: gin-service

springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
```

### 生產環境設定

建議在生產環境停用 Swagger UI：

```yaml
# application-prod.yml
springdoc:
  swagger-ui:
    enabled: false
  api-docs:
    enabled: false
```

啟動時使用：
```bash
./gradlew :mp-service:bootRun --args='--spring.profiles.active=prod'
```

## API 文件

### Swagger UI

啟動服務後，可透過瀏覽器存取 Swagger UI：

| Service | Swagger UI | API Docs (JSON) |
|---------|------------|-----------------|
| mp-service | http://localhost:8080/swagger-ui.html | http://localhost:8080/v3/api-docs |
| gbp-service | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |
| gin-service | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |

### 使用 Swagger UI 測試 API

1. 開啟 Swagger UI（如 http://localhost:8080/swagger-ui.html）
2. 展開想測試的端點（如 GET /api/process）
3. 點擊 **Try it out** 按鈕
4. 填入必要參數（如有）
5. 點擊 **Execute** 按鈕
6. 查看回應狀態碼、內容與執行時間

### 下載 API 規格

```bash
# JSON 格式
curl http://localhost:8080/v3/api-docs

# YAML 格式
curl http://localhost:8080/v3/api-docs.yaml
```

### H2 Database Console

mp-service 啟動後，可透過瀏覽器存取 H2 Console：

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:mpdb`
- Username: `sa`
- Password: (空白)

## 測試方法

### 執行單元測試

```bash
# 執行所有測試
./gradlew test

# 執行特定服務測試
./gradlew :mp-service:test
./gradlew :gbp-service:test
./gradlew :gin-service:test
```

### 執行整合測試

```bash
./gradlew integrationTest
```

### 測試覆蓋率報告

```bash
./gradlew jacocoTestReport
```

報告位置：`{service}/build/reports/jacoco/test/html/index.html`

### 手動測試斷路器

1. 啟動所有三個服務
2. 呼叫 `/api/process` 確認正常運作
3. 停止 gin-service
4. 連續呼叫 `/api/process` 三次以上
5. 觀察回應變為 degraded=true
6. 呼叫 `/api/status` 確認 circuitState=OPEN
7. 重新啟動 gin-service
8. 等待重試間隔後，觀察斷路器恢復

### 模擬連線失敗

```bash
# 停止 gin-service 後呼叫
curl http://localhost:8080/api/process
# 預期回應：{"success":true,"degraded":true,...}

# 查詢待傳送訊息數量
curl http://localhost:8080/api/status
# 預期回應：{"circuitState":"OPEN","pendingMessages":1,...}
```

## 專案結構

```
circuit-breaker/
├── build.gradle.kts              # 根專案 Gradle 配置
├── settings.gradle.kts           # 子模組設定
├── gradlew                       # Gradle Wrapper
├── gradlew.bat
├── README.md                     # 本文件
│
├── mp-service/                   # 主服務
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── java/com/circuitbreaker/mpservice/
│       │   │   ├── MpServiceApplication.java
│       │   │   ├── adapter/
│       │   │   │   └── in/rest/
│       │   │   │       ├── ProcessController.java
│       │   │   │       └── dto/
│       │   │   │           ├── ProcessResponse.java
│       │   │   │           ├── StatusResponse.java
│       │   │   │           └── ErrorResponse.java
│       │   │   └── infrastructure/
│       │   │       └── config/
│       │   │           └── OpenApiConfig.java
│       │   └── resources/
│       │       └── application.yml
│       └── test/
│
├── gbp-service/                  # 時間提供服務
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── java/com/circuitbreaker/gbpservice/
│       │   │   ├── GbpServiceApplication.java
│       │   │   ├── adapter/
│       │   │   │   └── in/rest/
│       │   │   │       ├── TimeController.java
│       │   │   │       └── dto/
│       │   │   │           └── TimeResponse.java
│       │   │   └── infrastructure/
│       │   │       └── config/
│       │   │           └── OpenApiConfig.java
│       │   └── resources/
│       │       └── application.yml
│       └── test/
│
├── gin-service/                  # 時間接收服務
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── java/com/circuitbreaker/ginservice/
│       │   │   ├── GinServiceApplication.java
│       │   │   ├── adapter/
│       │   │   │   └── in/rest/
│       │   │   │       ├── TimeController.java
│       │   │   │       └── dto/
│       │   │   │           ├── TimeRequest.java
│       │   │   │           └── AckResponse.java
│       │   │   └── infrastructure/
│       │   │       └── config/
│       │   │           └── OpenApiConfig.java
│       │   └── resources/
│       │       └── application.yml
│       └── test/
│
└── .specify/                     # 規格文件
    ├── memory/
    │   └── constitution.md       # 專案準則
    └── features/
        ├── 1-circuit-breaker-resilience/
        │   ├── spec.md           # 功能規格
        │   ├── plan.md           # 實作計畫
        │   ├── data-model.md     # 資料模型
        │   └── contracts/
        │       └── openapi.yaml  # API 規格
        └── 2-swagger-api-docs/
            ├── spec.md
            ├── plan.md
            └── tasks.md
```

## 技術棧

| 項目 | 技術 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Build Tool | Gradle 8.5 |
| Database | H2 (Embedded) |
| ORM | Spring Data JPA |
| HTTP Client | Spring WebFlux WebClient |
| API Docs | SpringDoc OpenAPI 2.3.0 |
| Testing | JUnit 5, Spring Boot Test |

## 設計原則

本專案遵循以下設計原則：

- **SOLID 原則**：依賴反轉、介面隔離、單一職責
- **六角形架構**：分離領域邏輯與基礎設施
- **DDD 領域驅動設計**：聚合根、領域事件、值物件
- **Framework Isolation**：領域層無框架依賴

## License

MIT License
