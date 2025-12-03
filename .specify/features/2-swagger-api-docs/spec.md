# Feature Specification: Swagger API 文件

**Feature ID:** 2-swagger-api-docs
**Created:** 2025-12-03
**Last Updated:** 2025-12-03
**Status:** Draft

## Summary

提供互動式 API 文件介面，讓使用者可以瀏覽、理解並直接測試三個微服務（mp-service、gbp-service、gin-service）的 API 端點。透過標準化的 API 文件，降低 API 整合的學習成本，並提供即時驗證 API 行為的能力。

## Business Context

### Problem Statement

目前三個微服務的 API 缺乏統一且互動式的文件，導致：
- 使用者需要閱讀程式碼或詢問開發人員才能了解 API 規格
- API 測試需要額外工具（如 Postman、curl），增加學習成本
- API 變更時文件容易過時，造成整合錯誤

### User Stories

1. As a **API 使用者**, I want **透過網頁介面瀏覽 API 規格**, so that **快速了解可用的端點與請求格式**.

2. As a **開發人員**, I want **直接在文件介面中測試 API**, so that **驗證 API 行為而無需切換工具**.

3. As a **系統整合者**, I want **取得 API 規格的機器可讀格式**, so that **自動產生客戶端程式碼**.

### Success Metrics

- 使用者可在 30 秒內找到特定 API 端點的文件
- 使用者可在 1 分鐘內完成一次 API 測試呼叫
- API 文件與實際 API 行為 100% 一致
- 所有三個服務的 API 文件可從單一入口存取

## Functional Requirements

### FR-1: API 文件介面

**Description:** 每個服務提供互動式 API 文件網頁介面
**Priority:** Must Have
**Acceptance Criteria:**
- [ ] 每個服務提供 `/swagger-ui` 或類似路徑的文件介面
- [ ] 介面顯示所有可用的 API 端點
- [ ] 每個端點顯示 HTTP 方法、路徑、描述
- [ ] 每個端點顯示請求參數與回應格式
- [ ] 介面提供範例請求與回應

### FR-2: 互動式測試功能

**Description:** 使用者可直接在文件介面中執行 API 呼叫
**Priority:** Must Have
**Acceptance Criteria:**
- [ ] 使用者可輸入請求參數
- [ ] 使用者可執行實際 API 呼叫
- [ ] 介面顯示回應狀態碼與內容
- [ ] 介面顯示回應時間
- [ ] 錯誤回應清楚顯示錯誤訊息

### FR-3: API 規格匯出

**Description:** 提供機器可讀的 API 規格格式
**Priority:** Should Have
**Acceptance Criteria:**
- [ ] 提供標準格式的 API 規格檔案（如 OpenAPI/Swagger JSON 或 YAML）
- [ ] 規格檔案可透過 URL 存取
- [ ] 規格檔案包含所有端點的完整定義

### FR-4: 服務資訊顯示

**Description:** 文件介面顯示服務基本資訊
**Priority:** Should Have
**Acceptance Criteria:**
- [ ] 顯示服務名稱與版本
- [ ] 顯示服務描述
- [ ] 顯示聯絡資訊或文件連結

## Non-Functional Requirements

### Performance (P8 Compliance)

- 文件介面載入時間：< 2 秒
- 測試呼叫執行後回應顯示：< API 回應時間 + 500ms

### Accessibility (P7 Compliance)

- 文件介面支援鍵盤導航
- 文字對比度符合 WCAG 2.1 AA 標準

## Behavior Specifications (P3 - BDD Compliance)

### Feature: API 文件瀏覽與測試

```gherkin
Feature: API 文件瀏覽與測試
  As a API 使用者
  I want 透過互動式文件介面了解和測試 API
  So that 快速整合服務而無需額外工具

  Scenario: 瀏覽 API 端點列表
    Given 使用者開啟 mp-service 的 API 文件介面
    When 文件載入完成
    Then 使用者看到所有可用的 API 端點
    And 每個端點顯示 HTTP 方法和路徑

  Scenario: 查看端點詳細資訊
    Given 使用者在 API 文件介面
    When 使用者點擊 GET /api/process 端點
    Then 顯示端點的詳細描述
    And 顯示請求參數說明
    And 顯示回應格式與範例

  Scenario: 執行測試呼叫
    Given 使用者在 GET /api/process 端點頁面
    When 使用者點擊「執行」或「Try it out」按鈕
    Then 系統發送實際 API 請求
    And 顯示回應狀態碼
    And 顯示回應內容

  Scenario: 下載 API 規格
    Given 使用者需要 API 規格檔案
    When 使用者存取規格檔案 URL
    Then 取得標準格式的 API 規格
    And 規格包含所有端點定義
```

## Technical Constraints

### Framework Isolation (P6 Compliance)

- API 文件功能屬於基礎設施層
- 文件生成邏輯不得影響領域層

### SOLID Considerations (P5 Compliance)

- 文件配置與核心服務邏輯分離
- 文件功能可獨立啟用或停用

## Testing Requirements (P2 Compliance)

### Integration Test Scenarios

- 文件介面可正常載入
- 測試呼叫功能正確執行
- 規格檔案格式正確

## Out of Scope

- 多語言支援（僅英文介面）
- 使用者認證（文件為公開存取）
- API 版本管理介面
- 自訂文件主題樣式

## Dependencies

- 依賴各服務的 API 端點實作
- 依賴 1-circuit-breaker-resilience 功能的 API 完成

## Assumptions

- 所有三個服務都需要 API 文件
- 開發與測試環境皆需提供文件介面
- 文件介面為公開存取，無需認證

## Appendix

### Related Documents

- 1-circuit-breaker-resilience/contracts/openapi.yaml: 現有 API 契約定義

### Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 0.1 | 2025-12-03 | Claude | Initial draft |
