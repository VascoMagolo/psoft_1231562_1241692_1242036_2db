package aisafe.shared.infrastructure.messaging;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * An event waiting to be published on RabbitMQ (transactional outbox).
 *
 * <p>It is written in the same transaction as the change it describes, so a change is never
 * committed without its event, and the event stays here until the broker confirms it.</p>
 */
@Entity
@Table(name = "event_outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventOutboxEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String exchange;

    @Column(nullable = false)
    private String routingKey;

    @Lob
    @Column(nullable = false)
    private String body;

    private String correlationId;

    @Column(nullable = false)
    private Instant createdAt;

    private int attempts;

    @Column(length = 500)
    private String lastError;

    public EventOutboxEntry(String exchange, String routingKey, String body, String correlationId) {
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.body = body;
        this.correlationId = correlationId;
        this.createdAt = Instant.now();
    }

    void failed(String error) {
        attempts++;
        lastError = error == null ? null : error.substring(0, Math.min(error.length(), 500));
    }
}
