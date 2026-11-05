package aisafe.shared.infrastructure.messaging;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventOutboxRepository extends JpaRepository<EventOutboxEntry, Long> {
    List<EventOutboxEntry> findTop100ByOrderByIdAsc();
}
