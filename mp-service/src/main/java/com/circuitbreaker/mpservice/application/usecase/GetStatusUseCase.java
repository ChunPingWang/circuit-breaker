package com.circuitbreaker.mpservice.application.usecase;

import com.circuitbreaker.mpservice.domain.model.CircuitState;

/**
 * 取得系統狀態 Use Case 介面
 * Input Port - 定義於 Application 層
 */
public interface GetStatusUseCase {

    /**
     * 取得系統狀態
     * @return 系統狀態
     */
    StatusResult getStatus();

    /**
     * 系統狀態結果
     */
    record StatusResult(
        CircuitState circuitState,
        int pendingMessages,
        boolean gbpServiceAvailable,
        boolean ginServiceAvailable
    ) {}
}
