package com.circuitbreaker.ginservice.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "確認回應")
public class AckResponse {

    @Schema(description = "是否成功接收", example = "true")
    private boolean received;

    @Schema(description = "接收時間", example = "2025-12-03T10:30:01Z")
    private String receivedAt;

    @Schema(description = "確認訊息", example = "Time data received successfully")
    private String message;

    public AckResponse() {
    }

    public AckResponse(boolean received, String receivedAt, String message) {
        this.received = received;
        this.receivedAt = receivedAt;
        this.message = message;
    }

    public boolean isReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }

    public String getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(String receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
