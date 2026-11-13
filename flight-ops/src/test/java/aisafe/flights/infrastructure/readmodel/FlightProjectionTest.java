package aisafe.flights.infrastructure.readmodel;

import aisafe.flights.domain.FlightStatus;
import aisafe.flights.infrastructure.replication.FlightReplica;
import aisafe.flights.infrastructure.replication.FlightReplicaHandler;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class FlightProjectionTest {

    private final FlightReplicaHandler handler = mock(FlightReplicaHandler.class);
    private final JsonMapper json = JsonMapper.builder().build();
    private final FlightProjection projection = new FlightProjection(handler, json);

    @Test
    void ensureProjectionEntityAndHandler() {
        assertEquals("flight", projection.entity());
        assertSame(handler, projection.handler());
    }

    @Test
    void ensureViewConvertsJsonNodeToFlightReplica() {
        OffsetDateTime departure = OffsetDateTime.of(2027, 6, 1, 8, 30, 0, 0, ZoneOffset.UTC);
        FlightReplica original = new FlightReplica(200L, departure, departure.plusHours(1),
                FlightStatus.SCHEDULED, "LIS", "MAD", "CS-TKA", 3L);
        JsonNode jsonNode = json.valueToTree(original);

        Object view = projection.view(jsonNode);

        assertInstanceOf(FlightReplica.class, view);
        FlightReplica converted = (FlightReplica) view;
        assertEquals(200L, converted.id());
        assertEquals("LIS", converted.originIataCode());
        assertEquals("MAD", converted.destinationIataCode());
        assertEquals("CS-TKA", converted.aircraftRegistration());
        assertEquals(FlightStatus.SCHEDULED, converted.status());
        assertEquals(3L, converted.version());
    }
}
