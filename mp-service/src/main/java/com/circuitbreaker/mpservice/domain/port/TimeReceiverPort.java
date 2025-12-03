package com.circuitbreaker.mpservice.domain.port;

/**
 * 時間接收者輸出埠
 * 用於將時間資料傳送至 gin-service
 * 定義於 Domain 層，由 Infrastructure 層實作
 */
public interface TimeReceiverPort {

    /**
     * 將時間資料傳送至 gin-service
     * @param timeData 時間資料
     * @return true 如果傳送成功
     */
    boolean sendTime(String timeData);

    /**
     * 檢查服務是否可用
     */
    boolean isAvailable();
}
