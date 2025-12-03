package com.circuitbreaker.mpservice.domain.port;

import com.circuitbreaker.mpservice.domain.model.PendingMessage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 訊息儲存庫輸出埠
 * 定義於 Domain 層，由 Infrastructure 層實作
 */
public interface MessageRepository {

    /**
     * 儲存待傳遞訊息
     */
    PendingMessage save(PendingMessage message);

    /**
     * 根據 ID 查詢訊息
     */
    Optional<PendingMessage> findById(UUID id);

    /**
     * 查詢所有待傳遞訊息（按建立時間排序）
     */
    List<PendingMessage> findAllPending();

    /**
     * 取得待傳遞訊息數量
     */
    int countPending();

    /**
     * 刪除訊息
     */
    void delete(PendingMessage message);
}
