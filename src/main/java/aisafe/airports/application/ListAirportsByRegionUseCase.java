package aisafe.airports.application;

import aisafe.shared.application.UseCase;
import org.springframework.transaction.annotation.Transactional;
import aisafe.airports.application.dtos.AirportGroupResponse;
import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Use case for listing airports grouped by region or country.
 */
@UseCase
@Transactional(readOnly = true)
public class ListAirportsByRegionUseCase {
    private final AirportRepository airportRepository;

    public ListAirportsByRegionUseCase(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    /**
     * Lists airports grouped by the specified criteria.
     * @param groupBy the criteria to group by, either "region" or "country"
     * @return a list of airport groups, each containing the group name and the airports in that group
     */
    public List<AirportGroupResponse> execute(String groupBy) {
        List<Airport> airports = airportRepository.findAll();
        // Group airports by the specified criteria
        Map<String, List<Airport>> grouped;
        if ("country".equalsIgnoreCase(groupBy)) {
            grouped = airports.stream().collect(Collectors.groupingBy(Airport::getCountry));
        } else {
            grouped = airports.stream().collect(
                    Collectors.groupingBy(a -> a.getRegion() != null ? a.getRegion() : "Unknown")
            );
        }
        // Sort groups by name and convert to DTOs
        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new AirportGroupResponse(
                        e.getKey(),
                        e.getValue().stream().map(AirportResponse::from).toList()
                ))
                .toList();
    }
}
