package com.circuitbreaker.mpservice.infrastructure.scheduler;

import com.circuitbreaker.mpservice.application.service.ProcessTimeService;
import com.circuitbreaker.mpservice.application.usecase.ReplayMessagesUseCase;
import com.circuitbreaker.mpservice.domain.model.CircuitState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MessageReplayScheduler {

    private static final Logger log = LoggerFactory.getLogger(MessageReplayScheduler.class);

    private final ProcessTimeService processTimeService;

    public MessageReplayScheduler(ProcessTimeService processTimeService) {
        this.processTimeService = processTimeService;
    }

    /**
     * 定期檢查斷路器狀態並嘗試補發暫存訊息
     * 使用 fixedDelay 確保序列執行，避免重複
     */
    @Scheduled(fixedDelayString = "${circuit-breaker.retry-interval-seconds:5}000")
    public void checkAndReplayMessages() {
        CircuitState state = processTimeService.getCircuitBreaker().getState();

        // 只在斷路器允許請求時嘗試補發
        if (processTimeService.getCircuitBreaker().allowRequest()) {
            log.debug("Circuit breaker state: {}, checking for pending messages", state);

            ReplayMessagesUseCase.ReplayResult result = processTimeService.replayPendingMessages();

            if (result.totalPending() > 0) {
                log.info("Replay result: {}/{} messages delivered, all delivered: {}",
                        result.successCount(), result.totalPending(), result.allDelivered());
            }
        } else {
            log.debug("Circuit breaker is OPEN, skipping replay check");
        }
    }
}
