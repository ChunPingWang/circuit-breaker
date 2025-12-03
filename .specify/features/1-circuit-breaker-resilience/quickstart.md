# Quickstart: 微服務斷路器韌性機制

**Feature ID:** 1-circuit-breaker-resilience
**Created:** 2025-12-03

## Prerequisites

- Java 17+
- Gradle 8.x

## Project Setup

### 1. Initialize Gradle Multi-Module Project

```bash
# 在專案根目錄執行
gradle init --type basic --dsl kotlin

# 建立模組目錄結構
mkdir -p shared/domain/src/main/java
mkdir -p shared/domain/src/test/java
mkdir -p mp-service/src/main/java
mkdir -p mp-service/src/main/resources
mkdir -p mp-service/src/test/java
mkdir -p gbp-service/src/main/java
mkdir -p gbp-service/src/main/resources
mkdir -p gbp-service/src/test/java
mkdir -p gin-service/src/main/java
mkdir -p gin-service/src/main/resources
mkdir -p gin-service/src/test/java
```

### 2. Configure settings.gradle.kts

```kotlin
rootProject.name = "circuit-breaker"

include("shared:domain")
include("mp-service")
include("gbp-service")
include("gin-service")
```

### 3. Configure Root build.gradle.kts

```kotlin
plugins {
    java
    id("org.springframework.boot") version "3.2.0" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
}

allprojects {
    group = "com.circuitbreaker"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
```

## Running Services

### Start All Services

```bash
# Terminal 1: gbp-service
./gradlew :gbp-service:bootRun

# Terminal 2: gin-service
./gradlew :gin-service:bootRun

# Terminal 3: mp-service
./gradlew :mp-service:bootRun
```

### Service Ports

| Service | Port | Health Check |
|---------|------|--------------|
| mp-service | 8080 | http://localhost:8080/actuator/health |
| gbp-service | 8081 | http://localhost:8081/actuator/health |
| gin-service | 8082 | http://localhost:8082/actuator/health |

## Testing the API

### Normal Flow

```bash
# 呼叫主服務處理時間
curl -X GET http://localhost:8080/api/process

# Expected Response:
# {
#   "success": true,
#   "degraded": false,
#   "timeData": "2025-12-03T10:30:00Z",
#   "message": "Time processed successfully"
# }
```

### Simulate Degraded Mode

```bash
# 停止 gin-service 後呼叫
curl -X GET http://localhost:8080/api/process

# Expected Response (HTTP 202):
# {
#   "success": true,
#   "degraded": true,
#   "timeData": "2025-12-03T10:30:00Z",
#   "message": "Time data queued for later delivery",
#   "pendingCount": 1
# }
```

### Check Status

```bash
curl -X GET http://localhost:8080/api/status

# Expected Response:
# {
#   "circuitState": "OPEN",
#   "pendingMessages": 5,
#   "lastFailureAt": "2025-12-03T10:30:00Z"
# }
```

## Running Tests

```bash
# Run all tests
./gradlew test

# Run with coverage report
./gradlew test jacocoTestReport

# Run BDD tests only
./gradlew :mp-service:cucumberTest
```

## Configuration

### mp-service application.yml

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop

services:
  gbp-service:
    url: http://localhost:8081
  gin-service:
    url: http://localhost:8082

circuit-breaker:
  failure-threshold: 3
  success-threshold: 2
  timeout-seconds: 5
  retry-interval-seconds: 5
  retry-duration-seconds: 60
```

## Package Structure

```
com.circuitbreaker
├── domain                          # shared/domain module
│   ├── circuitbreaker
│   │   ├── CircuitBreaker.java
│   │   └── CircuitState.java
│   ├── message
│   │   ├── PendingMessage.java
│   │   ├── MessageStatus.java
│   │   └── MessageQueue.java
│   └── event
│       ├── DomainEvent.java
│       ├── ServiceDisconnected.java
│       ├── ServiceReconnected.java
│       ├── MessageQueued.java
│       └── MessageDelivered.java
│
├── mpservice                       # mp-service module
│   ├── application
│   │   ├── port
│   │   │   ├── in
│   │   │   │   └── ProcessTimeUseCase.java
│   │   │   └── out
│   │   │       ├── TimeProviderPort.java
│   │   │       ├── TimeReceiverPort.java
│   │   │       └── MessageRepositoryPort.java
│   │   └── service
│   │       └── ProcessTimeService.java
│   └── infrastructure
│       ├── adapter
│       │   ├── in
│       │   │   └── rest
│       │   │       └── ProcessController.java
│       │   └── out
│       │       ├── persistence
│       │       │   ├── JpaMessageRepository.java
│       │       │   └── MessageEntity.java
│       │       └── http
│       │           ├── GbpServiceClient.java
│       │           └── GinServiceClient.java
│       └── config
│           └── AppConfig.java
│
├── gbpservice                      # gbp-service module
│   └── ...
│
└── ginservice                      # gin-service module
    └── ...
```

## Development Workflow

1. **Domain First**: 先實作 shared/domain 模組的領域邏輯
2. **Port Definition**: 定義 application 層的 port 介面
3. **Service Implementation**: 實作 application service
4. **Adapters**: 實作 infrastructure 層的 adapters
5. **Integration**: 整合測試驗證

## Troubleshooting

### H2 Console Access

URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: (empty)

### Circuit Breaker Not Opening

確認 gin-service 確實無法連線（檢查 port 8082），並且已發生至少 3 次連續失敗。

### Pending Messages Not Replaying

確認：
1. gin-service 已恢復運作
2. 斷路器處於 HALF_OPEN 或 CLOSED 狀態
3. 排程任務正在執行（檢查 logs）
