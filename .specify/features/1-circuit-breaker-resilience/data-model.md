# Data Model: 微服務斷路器韌性機制

**Feature ID:** 1-circuit-breaker-resilience
**Created:** 2025-12-03

## Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         mp-service                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────┐         ┌─────────────────────────────┐   │
│  │  CircuitBreaker │         │      PendingMessage         │   │
│  ├─────────────────┤         ├─────────────────────────────┤   │
│  │ - state         │         │ - id: UUID (PK)             │   │
│  │ - failureCount  │ 1    *  │ - timeData: String          │   │
│  │ - successCount  │◄───────►│ - status: MessageStatus     │   │
│  │ - lastFailureAt │         │ - createdAt: Instant        │   │
│  └─────────────────┘         │ - sentAt: Instant?          │   │
│                              └─────────────────────────────┘   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## Entities

### PendingMessage

**Description:** 待傳遞訊息實體，代表因 gin-service 斷線而暫存的時間資料。

**Identity:** UUID (系統生成)

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| id | UUID | No | 唯一識別碼 (Primary Key) |
| timeData | String | No | 從 gbp-service 取得的時間資料 |
| status | MessageStatus | No | 訊息狀態 |
| createdAt | Instant | No | 訊息建立時間 |
| sentAt | Instant | Yes | 成功傳送時間（SENT 狀態才有值） |

**Validation Rules:**
- id: 必須為有效 UUID
- timeData: 不可為空或空白
- status: 必須為有效的 MessageStatus 值
- createdAt: 必須為過去或現在的時間
- sentAt: 若存在，必須 >= createdAt

**State Transitions:**

```
           create
    ┌──────────────►  PENDING
    │                    │
    │                    │ send success
    │                    ▼
    │                  SENT
    │
    │ send failure (目前不使用，保留未來擴充)
    └──────────────►  FAILED
```

### CircuitBreaker (Domain Object, Non-Persistent)

**Description:** 斷路器領域物件，管理對 gin-service 呼叫的保護機制。此為記憶體物件，不持久化。

| Field | Type | Description |
|-------|------|-------------|
| state | CircuitState | 目前斷路器狀態 |
| failureCount | int | 連續失敗次數 |
| successCount | int | 半開狀態下連續成功次數 |
| lastFailureAt | Instant | 最後一次失敗時間 |

**State Machine:**

| Current State | Event | Next State | Condition |
|---------------|-------|------------|-----------|
| CLOSED | Call Failure | CLOSED | failureCount < 3 |
| CLOSED | Call Failure | OPEN | failureCount >= 3 |
| CLOSED | Call Success | CLOSED | reset failureCount |
| OPEN | Timer Expires | HALF_OPEN | 重試間隔到達 |
| HALF_OPEN | Probe Success | HALF_OPEN | successCount < 2 |
| HALF_OPEN | Probe Success | CLOSED | successCount >= 2 |
| HALF_OPEN | Probe Failure | OPEN | reset successCount |

## Value Objects

### MessageStatus

**Description:** 訊息狀態列舉

```java
public enum MessageStatus {
    PENDING,  // 等待傳送
    SENT,     // 已成功傳送
    FAILED    // 傳送失敗 (保留未來使用)
}
```

### CircuitState

**Description:** 斷路器狀態列舉

```java
public enum CircuitState {
    CLOSED,    // 正常運作，允許呼叫
    OPEN,      // 斷路開啟，拒絕呼叫
    HALF_OPEN  // 半開狀態，允許探測呼叫
}
```

### TimeData

**Description:** 時間資料值物件

```java
public record TimeData(
    String timestamp,    // ISO 8601 格式時間字串
    Instant receivedAt   // 接收時間
) {}
```

## Domain Events

### ServiceDisconnected

**Trigger:** gin-service 連線失敗且斷路器從 CLOSED 轉為 OPEN

```java
public record ServiceDisconnected(
    String serviceName,
    Instant occurredAt,
    int failureCount
) implements DomainEvent {}
```

### ServiceReconnected

**Trigger:** 斷路器從 HALF_OPEN 轉為 CLOSED（服務恢復）

```java
public record ServiceReconnected(
    String serviceName,
    Instant occurredAt
) implements DomainEvent {}
```

### MessageQueued

**Trigger:** 訊息暫存至資料庫

```java
public record MessageQueued(
    UUID messageId,
    String timeData,
    Instant occurredAt
) implements DomainEvent {}
```

### MessageDelivered

**Trigger:** 暫存訊息成功傳送至 gin-service

```java
public record MessageDelivered(
    UUID messageId,
    Instant deliveredAt
) implements DomainEvent {}
```

## Database Schema (H2)

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

## Repository Interface

```java
public interface PendingMessageRepository {

    PendingMessage save(PendingMessage message);

    Optional<PendingMessage> findById(UUID id);

    List<PendingMessage> findByStatusOrderByCreatedAtAsc(MessageStatus status);

    long countByStatus(MessageStatus status);
}
```

## Aggregate: MessageQueue

**Description:** 訊息佇列聚合根，管理 PendingMessage 的生命週期

**Invariants:**
1. 訊息必須依 createdAt 順序處理（FIFO）
2. SENT 狀態的訊息不可再次傳送
3. 補發失敗時必須停止，保持訊息順序

**Operations:**
- `enqueue(timeData)`: 建立新的 PENDING 訊息
- `markAsSent(messageId)`: 將訊息標記為 SENT
- `getPendingMessages()`: 取得所有待傳送訊息（依時間排序）
- `getPendingCount()`: 取得待傳送訊息數量
