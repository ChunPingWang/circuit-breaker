package com.circuitbreaker.mpservice.application.usecase;

/**
 * 處理時間流程 Use Case 介面
 * Input Port - 定義於 Application 層
 */
public interface ProcessTimeUseCase {

    /**
     * 執行時間處理流程
     * 1. 從 gbp-service 取得當前時間
     * 2. 嘗試將時間傳送至 gin-service
     * 3. 若 gin-service 無法連線，暫存資料
     *
     * @return 處理結果
     */
    ProcessResult process();

    /**
     * 處理結果
     */
    record ProcessResult(
        boolean success,
        boolean degraded,
        String timeData,
        String message,
        Integer pendingCount
    ) {}
}
