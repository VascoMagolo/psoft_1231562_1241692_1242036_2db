package aisafe.airports.application;

import aisafe.shared.application.UseCase;
import aisafe.airports.application.dtos.AirportGroupResponse;
import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@UseCase(readOnly = true)
public class ListAirportsByRegionUseCase {
    private final AirportRepository airportRepository;

    public ListAirportsByRegionUseCase(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public List<AirportGroupResponse> execute(String groupBy) {
        boolean byCountry = "country".equalsIgnoreCase(groupBy);
        List<Airport> airports = byCountry
                ? airportRepository.findAllOrderedByCountry()
                : airportRepository.findAllOrderedByRegion();

        return airports.stream()
                .collect(Collectors.groupingBy(
                        a -> byCountry ? a.getCountry() : (a.getRegion() != null ? a.getRegion() : "Unknown"),
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .entrySet().stream()
                .map(e -> new AirportGroupResponse(e.getKey(), e.getValue().stream().map(AirportResponse::from).toList()))
                .toList();
    }
}
