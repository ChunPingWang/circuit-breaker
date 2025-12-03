# Research: Swagger API 文件

**Feature ID:** 2-swagger-api-docs
**Created:** 2025-12-03

## Technology Decisions

### 1. API Documentation Library

**Decision:** SpringDoc OpenAPI

**Rationale:**
- Spring Boot 3 官方推薦的 OpenAPI 整合方案
- 支援 Jakarta EE 命名空間（javax.* → jakarta.*）
- 活躍的社群維護，持續更新
- 與 OpenAPI 3.0/3.1 標準完全相容

**Alternatives Considered:**
- Springfox Swagger：不支援 Spring Boot 3，停止維護
- 手動撰寫 OpenAPI YAML：無法自動同步，維護成本高
- Dokka (Kotlin)：本專案使用 Java，不適用

### 2. Swagger UI Version

**Decision:** SpringDoc 內建 Swagger UI（隨 springdoc-openapi-starter-webmvc-ui 版本）

**Rationale:**
- 與 SpringDoc 版本同步，避免相容性問題
- 無需額外配置靜態資源
- 自動整合 Spring Security（如有使用）

**Alternatives Considered:**
- 獨立部署 Swagger UI：增加維護複雜度
- ReDoc：美觀但缺乏測試功能
- Rapidoc：功能較新但社群較小

### 3. 文件生成策略

**Decision:** 註解驅動 + 程式碼自動掃描

**Rationale:**
- 文件與程式碼緊密耦合，減少不同步風險
- IDE 支援註解補全，提升開發效率
- 可結合現有 openapi.yaml 契約驗證一致性

**Alternatives Considered:**
- 契約優先（Contract First）：需額外工具生成程式碼
- 純程式碼生成（無註解）：缺乏詳細描述與範例

### 4. 多服務文件整合

**Decision:** 各服務獨立提供 Swagger UI

**Rationale:**
- 符合微服務獨立部署原則
- 各服務文件可獨立存取
- 避免單一入口成為瓶頸

**Alternatives Considered:**
- API Gateway 聚合：增加架構複雜度，超出當前需求
- 靜態文件合併：維護成本高，不易同步

## Best Practices Applied

### 1. 註解位置

```java
// Controller 類別層級
@Tag(name = "Process", description = "時間處理相關 API")
@RestController
@RequestMapping("/api")
public class ProcessController {

    // 方法層級
    @Operation(
        summary = "處理時間流程",
        description = "呼叫 gbp-service 取得時間，傳遞至 gin-service"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功"),
        @ApiResponse(responseCode = "202", description = "服務降級"),
        @ApiResponse(responseCode = "503", description = "服務不可用")
    })
    @GetMapping("/process")
    public ResponseEntity<ProcessResponse> process() { ... }
}
```

### 2. DTO 文件化

```java
@Schema(description = "處理結果回應")
public class ProcessResponse {

    @Schema(description = "是否成功", example = "true")
    private boolean success;

    @Schema(description = "是否降級模式", example = "false")
    private boolean degraded;

    @Schema(description = "時間資料", example = "2025-12-03T10:30:00Z")
    private String timeData;
}
```

### 3. 生產環境安全

```yaml
# application-prod.yml
springdoc:
  swagger-ui:
    enabled: false  # 生產環境停用
  api-docs:
    enabled: false  # 生產環境停用
```

## Resolved Clarifications

所有技術相關疑問已透過上述決策解決：
- ✅ 使用 SpringDoc OpenAPI 作為文件庫
- ✅ 採用註解驅動方式生成文件
- ✅ 各服務獨立提供 Swagger UI
- ✅ 生產環境可透過設定停用
