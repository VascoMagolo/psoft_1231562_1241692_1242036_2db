package aisafe.flightcopies.infrastructure;

import aisafe.flightcopies.application.RecordFlightCopyUseCase;
import aisafe.flightcopies.application.dtos.RecordFlightCopyRequest;
import aisafe.shared.infrastructure.messaging.CommandHandler;
import aisafe.shared.infrastructure.messaging.CommandPayloads;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * Contract C5 over RabbitMQ: flight-ops sends the copy of a flight as the command {@value #TYPE}
 * and waits for the answer. Same use case and same answers as {@code PUT /internal/flights/{id}}
 * ({@link FlightCopyController}): the copy stored (200), the route does not exist (404), or it is
 * not active (409), which makes flight-ops undo the scheduling (Saga).
 */
@Component
public class FlightCopyCommandHandler implements CommandHandler {

    public static final String TYPE = "flightcopy.record";

    private final RecordFlightCopyUseCase recordFlightCopy;
    private final CommandPayloads payloads;

    public FlightCopyCommandHandler(RecordFlightCopyUseCase recordFlightCopy, CommandPayloads payloads) {
        this.recordFlightCopy = recordFlightCopy;
        this.payloads = payloads;
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public Object handle(JsonNode payload) {
        return recordFlightCopy.execute(payloads.read(payload, RecordFlightCopyRequest.class));
    }
}
