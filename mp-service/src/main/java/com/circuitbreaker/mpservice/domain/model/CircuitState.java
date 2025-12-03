package com.circuitbreaker.mpservice.domain.model;

/**
 * 斷路器狀態列舉
 * - CLOSED: 正常狀態，請求正常通過
 * - OPEN: 開啟狀態，請求被阻擋
 * - HALF_OPEN: 半開狀態，允許探測請求通過以驗證服務是否恢復
 */
public enum CircuitState {
    CLOSED,
    OPEN,
    HALF_OPEN
}
