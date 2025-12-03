package com.circuitbreaker.mpservice.adapter.out.persistence;

import com.circuitbreaker.mpservice.domain.model.MessageStatus;
import com.circuitbreaker.mpservice.domain.model.PendingMessage;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pending_messages")
public class PendingMessageEntity {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "time_data", nullable = false)
    private String timeData;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MessageStatus status;

    @Column(name = "sent_at")
    private Instant sentAt;

    public PendingMessageEntity() {
    }

    public static PendingMessageEntity fromDomain(PendingMessage message) {
        PendingMessageEntity entity = new PendingMessageEntity();
        entity.id = message.getId();
        entity.timeData = message.getTimeData();
        entity.createdAt = message.getCreatedAt();
        entity.status = message.getStatus();
        entity.sentAt = message.getSentAt();
        return entity;
    }

    public PendingMessage toDomain() {
        return new PendingMessage(id, timeData, createdAt, status, sentAt);
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTimeData() {
        return timeData;
    }

    public void setTimeData(String timeData) {
        this.timeData = timeData;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }
}
