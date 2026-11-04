package aisafe.airports.infrastructure.readmodel;

import aisafe.airports.application.AirportListings;
import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.domain.AirportGroupingData;
import aisafe.airports.domain.AirportRepository;
import aisafe.shared.domain.PaginatedResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@link AirportListings} from this replica's airports, as in Assignment 1. When events are on,
 * the read model ({@link ReadModelAirportListings}) is used instead.
 */
@Component
@ConditionalOnProperty(name = "aisafe.events.enabled", havingValue = "false", matchIfMissing = true)
public class LocalAirportListings implements AirportListings {

    private final AirportRepository airportRepository;

    public LocalAirportListings(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    @Override
    public PaginatedResult<AirportResponse> search(String name, String city, String country, int pageNumber, int pageSize) {
        var result = airportRepository.searchAirports(name, city, country, pageNumber, pageSize);
        return new PaginatedResult<>(result.data().stream().map(AirportResponse::from).toList(), result.totalElements());
    }

    @Override
    public List<AirportGroupingData> groupedByRegion() {
        return airportRepository.findAllGroupingByRegion();
    }

    @Override
    public List<AirportGroupingData> groupedByCountry() {
        return airportRepository.findAllGroupingByCountry();
    }

    @Override
    public List<AirportResponse> findAll() {
        return airportRepository.findAll().stream().map(AirportResponse::from).toList();
    }
}
