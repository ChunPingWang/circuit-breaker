package com.circuitbreaker.mpservice.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "處理結果回應")
public class ProcessResponse {

    @Schema(description = "請求是否成功處理", example = "true")
    private boolean success;

    @Schema(description = "是否為降級模式", example = "false")
    private boolean degraded;

    @Schema(description = "從 gbp-service 取得的時間", example = "2025-12-03T10:30:00Z")
    private String timeData;

    @Schema(description = "處理結果訊息", example = "Time processed successfully")
    private String message;

    @Schema(description = "待傳送訊息數量（僅降級模式顯示）", example = "5", nullable = true)
    private Integer pendingCount;

    public ProcessResponse() {
    }

    public ProcessResponse(boolean success, boolean degraded, String timeData, String message, Integer pendingCount) {
        this.success = success;
        this.degraded = degraded;
        this.timeData = timeData;
        this.message = message;
        this.pendingCount = pendingCount;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isDegraded() {
        return degraded;
    }

    public void setDegraded(boolean degraded) {
        this.degraded = degraded;
    }

    public String getTimeData() {
        return timeData;
    }

    public void setTimeData(String timeData) {
        this.timeData = timeData;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Integer pendingCount) {
        this.pendingCount = pendingCount;
    }
}
