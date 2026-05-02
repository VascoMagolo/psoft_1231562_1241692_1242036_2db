package aisafe.airports.application;

import aisafe.UseCase;
import aisafe.airports.application.dtos.AirportGroupResponse;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UseCase
public class ListAirportsByRegionUseCase {
    private final AirportRepository airportRepository;

    public ListAirportsByRegionUseCase(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public List<AirportGroupResponse> execute(String groupBy) {
        List<Airport> airports = airportRepository.findAll();

        Map<String, List<Airport>> grouped;
        if ("country".equalsIgnoreCase(groupBy)) {
            grouped = airports.stream().collect(Collectors.groupingBy(Airport::getCountry));
        } else {
            grouped = airports.stream().collect(
                    Collectors.groupingBy(a -> a.getRegion() != null ? a.getRegion() : "Unknown")
            );
        }

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new AirportGroupResponse(e.getKey(), e.getValue()))
                .toList();
    }
}
