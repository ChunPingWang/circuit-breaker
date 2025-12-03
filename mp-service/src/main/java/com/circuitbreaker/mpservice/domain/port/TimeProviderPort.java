package com.circuitbreaker.mpservice.domain.port;

import java.util.Optional;

/**
 * 時間提供者輸出埠
 * 用於從 gbp-service 取得當前時間
 * 定義於 Domain 層，由 Infrastructure 層實作
 */
public interface TimeProviderPort {

    /**
     * 從 gbp-service 取得當前時間
     * @return 時間字串，若失敗則返回 empty
     */
    Optional<String> getCurrentTime();

    /**
     * 檢查服務是否可用
     */
    boolean isAvailable();
}
