package aisafe.airports.application;

import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.domain.AirportGroupingData;
import aisafe.shared.domain.PaginatedResult;

import java.util.List;

/**
 * The airport listings (the query side of the airports): search, grouping by region or country,
 * and every airport for the statistics. Without events they read this replica's airports (a
 * catalog: every replica holds every airport); with events, the read model.
 */
public interface AirportListings {

    /** Airports whose name, city and country contain the given text (ignoring case); null matches any. */
    PaginatedResult<AirportResponse> search(String name, String city, String country, int pageNumber, int pageSize);

    /** Every airport, ordered by region (no region counts as "Unknown"). */
    List<AirportGroupingData> groupedByRegion();

    /** Every airport, ordered by country. */
    List<AirportGroupingData> groupedByCountry();

    /** Every airport. */
    List<AirportResponse> findAll();
}
