package com.circuitbreaker.mpservice.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "錯誤回應")
public class ErrorResponse {

    @Schema(description = "錯誤代碼", example = "SERVICE_UNAVAILABLE")
    private String code;

    @Schema(description = "錯誤訊息", example = "gin-service is currently unavailable")
    private String message;

    @Schema(description = "錯誤發生時間", example = "2025-12-03T10:30:00Z")
    private String timestamp;

    public ErrorResponse() {
    }

    public ErrorResponse(String code, String message, String timestamp) {
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
