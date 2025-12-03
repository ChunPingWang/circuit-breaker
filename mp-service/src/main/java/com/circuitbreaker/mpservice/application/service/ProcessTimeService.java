package com.circuitbreaker.mpservice.application.service;

import com.circuitbreaker.mpservice.application.usecase.GetStatusUseCase;
import com.circuitbreaker.mpservice.application.usecase.ProcessTimeUseCase;
import com.circuitbreaker.mpservice.application.usecase.ReplayMessagesUseCase;
import com.circuitbreaker.mpservice.domain.model.PendingMessage;
import com.circuitbreaker.mpservice.domain.port.MessageRepository;
import com.circuitbreaker.mpservice.domain.port.TimeProviderPort;
import com.circuitbreaker.mpservice.domain.port.TimeReceiverPort;
import com.circuitbreaker.mpservice.domain.service.CircuitBreaker;

import java.util.List;
import java.util.Optional;

/**
 * 時間處理服務 - 應用層服務
 * 協調領域層物件與基礎設施層
 */
public class ProcessTimeService implements ProcessTimeUseCase, GetStatusUseCase, ReplayMessagesUseCase {

    private final TimeProviderPort timeProvider;
    private final TimeReceiverPort timeReceiver;
    private final MessageRepository messageRepository;
    private final CircuitBreaker circuitBreaker;

    public ProcessTimeService(
            TimeProviderPort timeProvider,
            TimeReceiverPort timeReceiver,
            MessageRepository messageRepository,
            CircuitBreaker circuitBreaker) {
        this.timeProvider = timeProvider;
        this.timeReceiver = timeReceiver;
        this.messageRepository = messageRepository;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public ProcessResult process() {
        // 1. 從 gbp-service 取得當前時間
        Optional<String> timeDataOpt = timeProvider.getCurrentTime();

        if (timeDataOpt.isEmpty()) {
            return new ProcessResult(
                false,
                false,
                null,
                "Failed to get time from gbp-service",
                null
            );
        }

        String timeData = timeDataOpt.get();

        // 2. 檢查斷路器狀態
        if (!circuitBreaker.allowRequest()) {
            // 斷路器開啟，直接暫存
            PendingMessage message = new PendingMessage(timeData);
            messageRepository.save(message);

            int pendingCount = messageRepository.countPending();

            return new ProcessResult(
                true,
                true,
                timeData,
                "Circuit breaker is open. Data queued for later delivery.",
                pendingCount
            );
        }

        // 3. 嘗試傳送至 gin-service
        boolean success = timeReceiver.sendTime(timeData);

        if (success) {
            circuitBreaker.recordSuccess();

            return new ProcessResult(
                true,
                false,
                timeData,
                "Time processed successfully",
                null
            );
        } else {
            // 傳送失敗
            circuitBreaker.recordFailure();

            // 暫存資料
            PendingMessage message = new PendingMessage(timeData);
            messageRepository.save(message);

            int pendingCount = messageRepository.countPending();

            return new ProcessResult(
                true,
                true,
                timeData,
                "gin-service unavailable. Data queued for later delivery.",
                pendingCount
            );
        }
    }

    @Override
    public StatusResult getStatus() {
        return new StatusResult(
            circuitBreaker.getState(),
            messageRepository.countPending(),
            timeProvider.isAvailable(),
            circuitBreaker.isClosed() ? timeReceiver.isAvailable() : false
        );
    }

    @Override
    public ReplayResult replayPendingMessages() {
        List<PendingMessage> pendingMessages = messageRepository.findAllPending();
        int totalPending = pendingMessages.size();
        int successCount = 0;

        for (PendingMessage message : pendingMessages) {
            boolean success = timeReceiver.sendTime(message.getTimeData());

            if (success) {
                message.markAsSent();
                messageRepository.save(message);
                successCount++;
                circuitBreaker.recordSuccess();
            } else {
                // 任一筆失敗則停止
                circuitBreaker.recordFailure();
                break;
            }
        }

        int failedCount = totalPending - successCount;
        boolean allDelivered = successCount == totalPending;

        return new ReplayResult(totalPending, successCount, failedCount, allDelivered);
    }

    /**
     * 取得斷路器實例（供排程使用）
     */
    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }
}
