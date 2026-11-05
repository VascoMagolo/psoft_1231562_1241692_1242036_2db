package aisafe.airports.infrastructure.readmodel;

import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportGroupingData;
import aisafe.airports.domain.Runway;
import aisafe.shared.infrastructure.readmodel.ReadModelStore;
import aisafe.shared.domain.PaginatedResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReadModelAirportListingsTest {

    private final JsonMapper json = JsonMapper.builder().build();
    private final ReadModelStore store = mock(ReadModelStore.class);
    private final ReadModelAirportListings listings = new ReadModelAirportListings(store, json);

    private ReadModelStore.Row row(String iata, String name, String city, String country, String region) {
        Airport airport = new Airport(iata, name, city, country, region, "UTC", 0.0, 0.0,
                List.of(new Runway("01/19", 2500, "010/190")));
        return new ReadModelStore.Row(iata, json.valueToTree(AirportResponse.from(airport)), null);
    }

    @BeforeEach
    void setUp() {
        // The store answers ordered by key (IATA code)
        when(store.findAll(AirportProjection.ENTITY)).thenReturn(List.of(
                row("CDG", "Charles de Gaulle", "Paris", "France", "Europe"),
                row("JFK", "John F. Kennedy", "New York", "USA", null),
                row("LIS", "Humberto Delgado", "Lisbon", "Portugal", "Europe"),
                row("OPO", "Francisco Sa Carneiro", "Porto", "Portugal", "Europe")));
    }

    @Test
    void ensureSearchMatchesPartOfTheTextIgnoringCase() {
        PaginatedResult<AirportResponse> result = listings.search(null, null, "portu", 0, 10);

        assertEquals(List.of("LIS", "OPO"), result.data().stream().map(AirportResponse::iataCode).toList());
        assertEquals(2, result.totalElements());
    }

    @Test
    void ensureSearchCombinesTheFilters() {
        assertEquals(List.of("OPO"), listings.search("carneiro", "PORTO", "Portugal", 0, 10).data().stream()
                .map(AirportResponse::iataCode).toList());
    }

    @Test
    void ensureSearchPagesTheResult() {
        PaginatedResult<AirportResponse> page = listings.search(null, null, null, 1, 3);

        assertEquals(List.of("OPO"), page.data().stream().map(AirportResponse::iataCode).toList());
        assertEquals(4, page.totalElements());
    }

    @Test
    void ensureGroupingByRegionPutsAirportsWithoutRegionAsUnknown() {
        assertEquals(List.of("CDG", "LIS", "OPO", "JFK"), listings.groupedByRegion().stream()
                .map(g -> g.iataCode().getCode()).toList());
    }

    @Test
    void ensureGroupingByCountry() {
        List<AirportGroupingData> rows = listings.groupedByCountry();

        assertEquals(List.of("France", "Portugal", "Portugal", "USA"), rows.stream().map(AirportGroupingData::country).toList());
    }
}
