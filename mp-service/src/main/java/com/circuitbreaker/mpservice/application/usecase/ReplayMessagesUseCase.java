package com.circuitbreaker.mpservice.application.usecase;

/**
 * 補發暫存訊息 Use Case 介面
 * Input Port - 定義於 Application 層
 */
public interface ReplayMessagesUseCase {

    /**
     * 補發所有待傳送訊息
     * 逐筆傳送，任一筆失敗則停止
     *
     * @return 補發結果
     */
    ReplayResult replayPendingMessages();

    /**
     * 補發結果
     */
    record ReplayResult(
        int totalPending,
        int successCount,
        int failedCount,
        boolean allDelivered
    ) {}
}
