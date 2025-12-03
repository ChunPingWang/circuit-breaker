package com.circuitbreaker.mpservice.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 待傳遞訊息實體
 * 包含時間資料、建立時間、傳遞狀態
 */
public class PendingMessage {

    private final UUID id;
    private final String timeData;
    private final Instant createdAt;
    private MessageStatus status;
    private Instant sentAt;

    public PendingMessage(String timeData) {
        this.id = UUID.randomUUID();
        this.timeData = Objects.requireNonNull(timeData, "timeData must not be null");
        this.createdAt = Instant.now();
        this.status = MessageStatus.PENDING;
        this.sentAt = null;
    }

    public PendingMessage(UUID id, String timeData, Instant createdAt, MessageStatus status, Instant sentAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.timeData = Objects.requireNonNull(timeData, "timeData must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.sentAt = sentAt;
    }

    public void markAsSent() {
        this.status = MessageStatus.SENT;
        this.sentAt = Instant.now();
    }

    public void markAsFailed() {
        this.status = MessageStatus.FAILED;
    }

    public boolean isPending() {
        return this.status == MessageStatus.PENDING;
    }

    public UUID getId() {
        return id;
    }

    public String getTimeData() {
        return timeData;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PendingMessage that = (PendingMessage) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PendingMessage{" +
                "id=" + id +
                ", timeData='" + timeData + '\'' +
                ", createdAt=" + createdAt +
                ", status=" + status +
                ", sentAt=" + sentAt +
                '}';
    }
}
