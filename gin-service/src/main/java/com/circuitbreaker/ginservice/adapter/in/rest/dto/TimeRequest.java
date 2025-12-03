package com.circuitbreaker.ginservice.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "時間傳送請求")
public class TimeRequest {

    @Schema(description = "時間資料 (ISO 8601 格式)", example = "2025-12-03T10:30:00Z", required = true)
    private String timeData;

    @Schema(description = "來源服務識別", example = "mp-service")
    private String source;

    public TimeRequest() {
    }

    public TimeRequest(String timeData, String source) {
        this.timeData = timeData;
        this.source = source;
    }

    public String getTimeData() {
        return timeData;
    }

    public void setTimeData(String timeData) {
        this.timeData = timeData;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
