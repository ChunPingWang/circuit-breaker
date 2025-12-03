# Feature Specification: 微服務斷路器韌性機制

**Feature ID:** 1-circuit-breaker-resilience
**Created:** 2025-12-03
**Last Updated:** 2025-12-03
**Status:** Draft

## Clarifications

### Session 2025-12-03

- Q: 斷路器應該在什麼條件下從「關閉」狀態轉為「開啟」狀態？ → A: 連續 3 次失敗後觸發
- Q: 呼叫 gin-service 等待多久無回應視為連線失敗？ → A: 5 秒
- Q: 斷路器在重試期間偵測到服務恢復後，應如何驗證服務穩定性？ → A: 連續 2 次探測成功後關閉斷路器
- Q: 暫存的待傳遞資料應保留多長時間？ → A: 永久保留直到成功傳送
- Q: 補發暫存資料時的批次策略與失敗處理？ → A: 逐筆傳送，任一筆失敗則停止補發

## Summary

本功能建立一個具備韌性的微服務架構，包含三個服務（mp-service、gbp-service、gin-service）。mp-service 作為主要入口點，透過斷路器模式處理下游服務（gin-service）的連線失敗情況，確保在服務中斷期間資料不遺失，並在服務恢復後自動補發暫存資料。

## Business Context

### Problem Statement

在分散式微服務架構中，下游服務可能因網路問題、服務重啟或過載而暫時無法回應。若無適當的容錯機制，將導致：
- 使用者請求失敗
- 資料遺失
- 連鎖故障擴散至整個系統

### User Stories

1. As a **系統使用者**, I want **呼叫 mp-service 時能獲得可靠回應**, so that **即使部分服務暫時不可用，我的請求仍能被妥善處理**.

2. As a **系統維運人員**, I want **服務能自動偵測並處理下游服務故障**, so that **減少人工介入的需求並提高系統可用性**.

3. As a **資料管理者**, I want **斷線期間的資料被安全保存並在恢復後補發**, so that **確保資料完整性不受服務中斷影響**.

### Success Metrics

- 服務可用性：mp-service 在 gin-service 斷線期間仍能正常接收請求，可用率維持 99.5% 以上
- 資料完整性：斷線期間暫存的資料 100% 成功補發至 gin-service
- 恢復時間：服務恢復後 30 秒內開始補發暫存資料
- 使用者體驗：使用者在服務降級期間收到明確的狀態回饋

## Functional Requirements

### FR-1: 時間查詢服務 (gbp-service)

**Description:** gbp-service 提供 API 端點，被呼叫時回傳當前系統時間，並記錄呼叫日誌。
**Priority:** Must Have
**Acceptance Criteria:**
- [ ] 提供 API 端點供 mp-service 呼叫
- [ ] 每次被呼叫時回傳當前系統時間
- [ ] 每次被呼叫時在 console 輸出時間日誌

### FR-2: 時間接收服務 (gin-service)

**Description:** gin-service 提供 API 端點接收時間資料輸入。
**Priority:** Must Have
**Acceptance Criteria:**
- [ ] 提供 API 端點接收時間資料
- [ ] 接收來自 mp-service 傳遞的時間資訊
- [ ] 成功處理後回傳確認

### FR-3: 主服務協調 (mp-service)

**Description:** mp-service 作為主要入口，協調呼叫 gbp-service 和 gin-service。
**Priority:** Must Have
**Acceptance Criteria:**
- [ ] 提供使用者呼叫的 API 端點
- [ ] 呼叫 gbp-service 取得當前時間
- [ ] 將取得的時間傳遞至 gin-service

### FR-4: 斷路器機制

**Description:** mp-service 對 gin-service 的呼叫實作斷路器模式，處理連線失敗情況。
**Priority:** Must Have
**Acceptance Criteria:**
- [ ] 呼叫 gin-service 等待 5 秒無回應視為連線失敗
- [ ] 連續 3 次連線失敗後觸發斷路器開啟
- [ ] 斷路器開啟期間，將請求資料暫存
- [ ] 定期嘗試重新連線（重試間隔可設定）
- [ ] 重試持續時間可設定
- [ ] 連續 2 次探測成功後關閉斷路器，恢復正常運作

### FR-5: 資料暫存機制

**Description:** gin-service 斷線期間，mp-service 將未傳遞的資料儲存至資料庫。
**Priority:** Must Have
**Acceptance Criteria:**
- [ ] 斷線時將待傳遞資料寫入資料庫
- [ ] 記錄資料的時間戳記與狀態
- [ ] 資料以待傳遞狀態標記
- [ ] 暫存資料永久保留直到成功傳送（不自動刪除或過期）

### FR-6: 資料補發機制

**Description:** gin-service 恢復連線後，mp-service 自動補發所有暫存資料。
**Priority:** Must Have
**Acceptance Criteria:**
- [ ] 連線恢復後自動查詢待傳遞資料
- [ ] 逐筆依序將暫存資料傳送至 gin-service
- [ ] 成功傳送後更新資料狀態為已傳遞
- [ ] 任一筆傳送失敗時立即停止補發，保留未傳送資料待下次重試

## Non-Functional Requirements

### Performance (P8 Compliance)

- 回應時間：正常情況下使用者請求於 500ms 內回應
- 吞吐量：支援每秒 100 個並發請求
- 資源限制：暫存資料庫大小上限可設定

### Security

- 服務間通訊使用內部網路
- 暫存資料不包含敏感個資

## Domain Model (P4 - DDD Compliance)

### Ubiquitous Language

| Term | Definition |
|------|------------|
| 斷路器 (Circuit Breaker) | 偵測服務故障並暫時中斷呼叫的保護機制，具有關閉、開啟、半開三種狀態 |
| 暫存資料 (Pending Message) | 因斷線而儲存於資料庫等待補發的資料 |
| 補發 (Replay) | 服務恢復後重新傳送暫存資料的動作 |
| 健康探測 (Health Probe) | 定期檢查下游服務可用性的機制 |
| 重試間隔 (Retry Interval) | 兩次重試嘗試之間的等待時間 |
| 重試持續時間 (Retry Duration) | 持續重試的總時間長度 |
| 連線逾時 (Connection Timeout) | 等待服務回應的最長時間，超過視為失敗（5 秒） |
| 半開狀態 (Half-Open) | 斷路器嘗試恢復時的過渡狀態，用於驗證服務穩定性 |

### Entities

- **PendingMessage**: 待傳遞訊息實體，以唯一 ID 識別，包含時間資料、建立時間、傳遞狀態

### Value Objects

- **Timestamp**: 時間戳記，不可變，以值比較相等性
- **MessageStatus**: 訊息狀態（Pending, Sent, Failed），以值比較

### Aggregates

- **MessageQueue**: 訊息佇列聚合根，管理 PendingMessage 的生命週期，確保訊息依序處理

### Domain Events

- **ServiceDisconnected**: gin-service 斷線時觸發
- **ServiceReconnected**: gin-service 恢復連線時觸發
- **MessageQueued**: 訊息暫存至資料庫時觸發
- **MessageDelivered**: 訊息成功傳遞時觸發

## Behavior Specifications (P3 - BDD Compliance)

### Feature: 微服務協調與斷路器韌性

```gherkin
Feature: 微服務協調與斷路器韌性
  As a 系統使用者
  I want mp-service 能可靠地協調下游服務
  So that 即使部分服務中斷，系統仍能正常運作

  Scenario: 正常流程 - 所有服務可用
    Given gbp-service 正常運作
    And gin-service 正常運作
    When 使用者呼叫 mp-service API
    Then mp-service 從 gbp-service 取得當前時間
    And gbp-service 在 console 輸出時間日誌
    And mp-service 將時間傳遞至 gin-service
    And 使用者收到成功回應

  Scenario: gin-service 斷線時的資料暫存
    Given gbp-service 正常運作
    And gin-service 無法連線
    When 使用者呼叫 mp-service API
    Then mp-service 從 gbp-service 取得當前時間
    And mp-service 偵測到 gin-service 無回應
    And mp-service 將時間資料儲存至資料庫
    And 使用者收到服務降級通知

  Scenario: gin-service 恢復後的資料補發
    Given 資料庫中有 3 筆待傳遞資料
    And gin-service 恢復連線
    When mp-service 偵測到 gin-service 可用
    Then mp-service 依序傳送 3 筆暫存資料至 gin-service
    And 所有資料狀態更新為已傳遞

  Scenario: 可設定的重試機制
    Given gin-service 無法連線
    And 重試間隔設定為 5 秒
    And 重試持續時間設定為 60 秒
    When 斷路器開啟
    Then mp-service 每 5 秒嘗試連線 gin-service
    And 持續重試直到 60 秒或連線成功
```

## Technical Constraints

### Framework Isolation (P6 Compliance)

- 領域邏輯（斷路器狀態管理、訊息佇列邏輯）不得依賴特定框架
- 基礎設施層負責：HTTP 用戶端、資料庫存取、日誌輸出

### SOLID Considerations (P5 Compliance)

- 斷路器實作應可獨立於特定 HTTP 用戶端（依賴反轉）
- 訊息儲存介面與實作分離（介面隔離）
- 各服務職責單一明確（單一職責）

## Testing Requirements (P2 Compliance)

### Unit Test Coverage

- 最低覆蓋率：80%
- 需 100% 覆蓋的關鍵路徑：斷路器狀態轉換、訊息暫存與補發邏輯

### Integration Test Scenarios

- 三服務正常協調流程
- gin-service 斷線偵測與資料暫存
- 服務恢復與資料補發

### Performance Test Benchmarks

- 正常情況 API 回應時間 < 500ms
- 斷線情況 API 回應時間 < 200ms（不等待 gin-service）
- 補發 100 筆資料 < 30 秒

## Out of Scope

- 使用者認證與授權機制
- 服務間加密通訊
- 分散式追蹤與監控儀表板
- 多實例負載平衡
- gbp-service 的斷路器保護（僅 gin-service 需要）

## Dependencies

- 資料庫：用於暫存待傳遞資料
- 內部網路：三個服務間的通訊

## Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| 暫存資料庫容量耗盡 | M | H | 設定資料保留政策與容量告警 |
| 補發期間資料順序錯亂 | L | M | 實作序列化補發機制 |
| 重試風暴造成資源耗盡 | M | M | 實作指數退避與最大重試限制 |

## Assumptions

- 三個服務部署於同一內部網路，網路延遲可忽略
- 資料庫服務高可用，不考慮資料庫故障情況
- 時間資料不需要精確到毫秒以下
- 單一 mp-service 實例運作，不考慮多實例競爭

## Appendix

### Related Documents

- PRD.md: 原始產品需求文件

### Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 0.1 | 2025-12-03 | Claude | Initial draft |
| 0.2 | 2025-12-03 | Claude | 新增釐清事項：斷路器觸發條件、連線逾時、半開狀態、資料保留、補發策略 |
