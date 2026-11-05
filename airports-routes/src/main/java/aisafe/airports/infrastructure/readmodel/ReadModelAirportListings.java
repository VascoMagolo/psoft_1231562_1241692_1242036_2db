package aisafe.airports.infrastructure.readmodel;

import aisafe.airports.application.AirportListings;
import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.domain.AirportGroupingData;
import aisafe.airports.domain.IataCode;
import aisafe.shared.infrastructure.readmodel.ReadModelStore;
import aisafe.shared.domain.PaginatedResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * {@link AirportListings} from this replica's read model, when events are on: each airport is kept
 * as the answer the clients get ({@link AirportResponse}), so a listing is read as it is, without
 * building the domain objects. Search and grouping follow the same rules as the write model's
 * queries ({@link LocalAirportListings}).
 */
@Component
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "true")
public class ReadModelAirportListings implements AirportListings {

    private final ReadModelStore store;
    private final ObjectMapper objectMapper;

    public ReadModelAirportListings(ReadModelStore store, ObjectMapper objectMapper) {
        this.store = store;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaginatedResult<AirportResponse> search(String name, String city, String country, int pageNumber, int pageSize) {
        List<AirportResponse> found = findAll().stream()
                .filter(a -> contains(a.name(), name) && contains(a.city(), city) && contains(a.country(), country))
                .toList();
        return PaginatedResult.pageOf(found, pageNumber, pageSize);
    }

    @Override
    public List<AirportGroupingData> groupedByRegion() {
        return grouping(Comparator.comparing(a -> a.region() == null ? "Unknown" : a.region()));
    }

    @Override
    public List<AirportGroupingData> groupedByCountry() {
        return grouping(Comparator.comparing(AirportResponse::country, Comparator.nullsFirst(Comparator.naturalOrder())));
    }

    /** Ordered by IATA code (the key of the read model). */
    @Override
    public List<AirportResponse> findAll() {
        return store.findAll(AirportProjection.ENTITY).stream()
                .map(row -> objectMapper.treeToValue(row.view(), AirportResponse.class))
                .toList();
    }

    private List<AirportGroupingData> grouping(Comparator<AirportResponse> order) {
        return findAll().stream()
                .sorted(order)
                .map(a -> new AirportGroupingData(new IataCode(a.iataCode()), a.name(), a.region(), a.country()))
                .toList();
    }

    private static boolean contains(String value, String text) {
        return text == null || (value != null && value.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT)));
    }
}
