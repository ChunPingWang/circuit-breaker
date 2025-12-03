package com.circuitbreaker.mpservice.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "系統狀態回應")
public class StatusResponse {

    @Schema(description = "斷路器狀態", example = "CLOSED", allowableValues = {"CLOSED", "OPEN", "HALF_OPEN"})
    private String circuitState;

    @Schema(description = "待傳送訊息數量", example = "3")
    private int pendingMessages;

    @Schema(description = "gbp-service 連線狀態", example = "true")
    private boolean gbpServiceAvailable;

    @Schema(description = "gin-service 連線狀態", example = "false")
    private boolean ginServiceAvailable;

    public StatusResponse() {
    }

    public StatusResponse(String circuitState, int pendingMessages, boolean gbpServiceAvailable, boolean ginServiceAvailable) {
        this.circuitState = circuitState;
        this.pendingMessages = pendingMessages;
        this.gbpServiceAvailable = gbpServiceAvailable;
        this.ginServiceAvailable = ginServiceAvailable;
    }

    public String getCircuitState() {
        return circuitState;
    }

    public void setCircuitState(String circuitState) {
        this.circuitState = circuitState;
    }

    public int getPendingMessages() {
        return pendingMessages;
    }

    public void setPendingMessages(int pendingMessages) {
        this.pendingMessages = pendingMessages;
    }

    public boolean isGbpServiceAvailable() {
        return gbpServiceAvailable;
    }

    public void setGbpServiceAvailable(boolean gbpServiceAvailable) {
        this.gbpServiceAvailable = gbpServiceAvailable;
    }

    public boolean isGinServiceAvailable() {
        return ginServiceAvailable;
    }

    public void setGinServiceAvailable(boolean ginServiceAvailable) {
        this.ginServiceAvailable = ginServiceAvailable;
    }
}
