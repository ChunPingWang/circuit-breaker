package com.circuitbreaker.mpservice.domain.model;

/**
 * 訊息狀態列舉
 * - PENDING: 待傳送
 * - SENT: 已傳送
 * - FAILED: 傳送失敗
 */
public enum MessageStatus {
    PENDING,
    SENT,
    FAILED
}
