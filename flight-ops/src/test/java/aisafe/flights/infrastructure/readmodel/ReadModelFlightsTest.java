package aisafe.flights.infrastructure.readmodel;

import aisafe.flights.domain.FlightStatus;
import aisafe.flights.domain.ScheduledFlight;
import aisafe.flights.infrastructure.replication.FlightReplica;
import aisafe.shared.application.Versioned;
import aisafe.shared.infrastructure.readmodel.ReadModelStore;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReadModelFlightsTest {

    private final JsonMapper json = JsonMapper.builder().build();
    private final ReadModelStore store = mock(ReadModelStore.class);
    private final ReadModelFlights readModel = new ReadModelFlights(store, json);

    private ReadModelStore.Row row(long id, String aircraft, String origin, String destination, FlightStatus status, Long version) {
        OffsetDateTime departure = OffsetDateTime.of(2027, 5, 1, 10, 0, 0, 0, ZoneOffset.UTC);
        FlightReplica replica = new FlightReplica(id, departure, departure.plusHours(2), status, origin, destination, aircraft, version);
        return new ReadModelStore.Row(String.valueOf(id), json.valueToTree(replica), version);
    }

    @Test
    void ensureFindAllMapsRowsToVersionedScheduledFlights() {
        when(store.findAll(FlightProjection.ENTITY)).thenReturn(List.of(
                row(101L, "CS-TPA", "LIS", "OPO", FlightStatus.SCHEDULED, 1L),
                row(102L, "CS-TPB", "OPO", "MAD", FlightStatus.CANCELED, 2L)
        ));

        List<Versioned<ScheduledFlight>> results = readModel.findAll();

        assertEquals(2, results.size());

        ScheduledFlight f1 = results.get(0).value();
        assertEquals(101L, f1.getId());
        assertEquals("CS-TPA", f1.getAircraftRegistrationNumber().getNumber());
        assertEquals("LIS", f1.getOriginCode().getCode());
        assertEquals("OPO", f1.getDestinationCode().getCode());
        assertEquals(FlightStatus.SCHEDULED, f1.getStatus());
        assertEquals(1L, results.get(0).version());

        ScheduledFlight f2 = results.get(1).value();
        assertEquals(102L, f2.getId());
        assertEquals("CS-TPB", f2.getAircraftRegistrationNumber().getNumber());
        assertEquals(FlightStatus.CANCELED, f2.getStatus());
        assertEquals(2L, results.get(1).version());
    }

    @Test
    void ensureEmptyStoreReturnsEmptyList() {
        when(store.findAll(FlightProjection.ENTITY)).thenReturn(List.of());

        List<Versioned<ScheduledFlight>> results = readModel.findAll();

        assertTrue(results.isEmpty());
    }
}
