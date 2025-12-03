package com.circuitbreaker.gbpservice.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "時間回應")
public class TimeResponse {

    @Schema(description = "當前時間 (ISO 8601 格式)", example = "2025-12-03T10:30:00Z")
    private String currentTime;

    @Schema(description = "時區", example = "UTC")
    private String timezone;

    public TimeResponse() {
    }

    public TimeResponse(String currentTime, String timezone) {
        this.currentTime = currentTime;
        this.timezone = timezone;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
