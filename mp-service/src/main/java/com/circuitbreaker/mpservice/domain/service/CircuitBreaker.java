package com.circuitbreaker.mpservice.domain.service;

import com.circuitbreaker.mpservice.domain.model.CircuitState;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 斷路器領域服務
 * 管理斷路器狀態轉換邏輯，純 Java 實作，無框架依賴
 *
 * 狀態轉換規則：
 * - CLOSED → OPEN: 連續失敗達到 failureThreshold
 * - OPEN → HALF_OPEN: 等待 retryInterval 後進行探測
 * - HALF_OPEN → CLOSED: 連續成功達到 successThreshold
 * - HALF_OPEN → OPEN: 探測失敗
 */
public class CircuitBreaker {

    private final int failureThreshold;
    private final int successThreshold;
    private final long retryIntervalMillis;

    private final AtomicReference<CircuitState> state;
    private final AtomicInteger consecutiveFailures;
    private final AtomicInteger consecutiveSuccesses;
    private final AtomicReference<Instant> lastFailureTime;

    public CircuitBreaker(int failureThreshold, int successThreshold, long retryIntervalSeconds) {
        if (failureThreshold <= 0) {
            throw new IllegalArgumentException("failureThreshold must be positive");
        }
        if (successThreshold <= 0) {
            throw new IllegalArgumentException("successThreshold must be positive");
        }
        if (retryIntervalSeconds <= 0) {
            throw new IllegalArgumentException("retryIntervalSeconds must be positive");
        }

        this.failureThreshold = failureThreshold;
        this.successThreshold = successThreshold;
        this.retryIntervalMillis = retryIntervalSeconds * 1000;

        this.state = new AtomicReference<>(CircuitState.CLOSED);
        this.consecutiveFailures = new AtomicInteger(0);
        this.consecutiveSuccesses = new AtomicInteger(0);
        this.lastFailureTime = new AtomicReference<>(null);
    }

    /**
     * 檢查是否允許請求通過
     * @return true 如果請求可以通過
     */
    public boolean allowRequest() {
        CircuitState currentState = state.get();

        switch (currentState) {
            case CLOSED:
                return true;

            case OPEN:
                // 檢查是否已經過了重試間隔
                Instant lastFailure = lastFailureTime.get();
                if (lastFailure != null) {
                    long elapsedMillis = Instant.now().toEpochMilli() - lastFailure.toEpochMilli();
                    if (elapsedMillis >= retryIntervalMillis) {
                        // 轉換到半開狀態
                        if (state.compareAndSet(CircuitState.OPEN, CircuitState.HALF_OPEN)) {
                            consecutiveSuccesses.set(0);
                        }
                        return true;
                    }
                }
                return false;

            case HALF_OPEN:
                // 半開狀態允許探測請求
                return true;

            default:
                return false;
        }
    }

    /**
     * 記錄請求成功
     */
    public void recordSuccess() {
        CircuitState currentState = state.get();

        switch (currentState) {
            case CLOSED:
                // 重置失敗計數
                consecutiveFailures.set(0);
                break;

            case HALF_OPEN:
                int successes = consecutiveSuccesses.incrementAndGet();
                if (successes >= successThreshold) {
                    // 達到成功閾值，關閉斷路器
                    if (state.compareAndSet(CircuitState.HALF_OPEN, CircuitState.CLOSED)) {
                        consecutiveFailures.set(0);
                        consecutiveSuccesses.set(0);
                        lastFailureTime.set(null);
                    }
                }
                break;

            case OPEN:
                // 開啟狀態下不應該有成功，忽略
                break;
        }
    }

    /**
     * 記錄請求失敗
     */
    public void recordFailure() {
        CircuitState currentState = state.get();
        lastFailureTime.set(Instant.now());

        switch (currentState) {
            case CLOSED:
                int failures = consecutiveFailures.incrementAndGet();
                if (failures >= failureThreshold) {
                    // 達到失敗閾值，開啟斷路器
                    state.compareAndSet(CircuitState.CLOSED, CircuitState.OPEN);
                }
                break;

            case HALF_OPEN:
                // 半開狀態下失敗，重新開啟斷路器
                if (state.compareAndSet(CircuitState.HALF_OPEN, CircuitState.OPEN)) {
                    consecutiveSuccesses.set(0);
                }
                break;

            case OPEN:
                // 已經開啟，保持狀態
                break;
        }
    }

    /**
     * 取得當前狀態
     */
    public CircuitState getState() {
        return state.get();
    }

    /**
     * 檢查斷路器是否開啟
     */
    public boolean isOpen() {
        return state.get() == CircuitState.OPEN;
    }

    /**
     * 檢查斷路器是否關閉
     */
    public boolean isClosed() {
        return state.get() == CircuitState.CLOSED;
    }

    /**
     * 取得連續失敗次數
     */
    public int getConsecutiveFailures() {
        return consecutiveFailures.get();
    }

    /**
     * 取得連續成功次數
     */
    public int getConsecutiveSuccesses() {
        return consecutiveSuccesses.get();
    }

    /**
     * 重置斷路器狀態（用於測試）
     */
    public void reset() {
        state.set(CircuitState.CLOSED);
        consecutiveFailures.set(0);
        consecutiveSuccesses.set(0);
        lastFailureTime.set(null);
    }
}
