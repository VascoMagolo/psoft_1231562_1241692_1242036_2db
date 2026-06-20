package aisafe.airports.application;

import aisafe.shared.application.UseCase;
import aisafe.airports.application.dtos.AirportGroupResponse;
import aisafe.airports.application.dtos.AirportGroupSummaryResponse;
import aisafe.airports.domain.AirportGroupingData;
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
        List<AirportGroupingData> rows = byCountry
                ? airportRepository.findAllGroupingByCountry()
                : airportRepository.findAllGroupingByRegion();

        return rows.stream()
                .collect(Collectors.groupingBy(
                        r -> byCountry ? r.country() : (r.region() != null ? r.region() : "Unknown"),
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .entrySet().stream()
                .map(e -> new AirportGroupResponse(e.getKey(), e.getValue().stream()
                        .map(r -> new AirportGroupSummaryResponse(r.iataCode().getCode(), r.name(), r.region(), r.country()))
                        .toList()))
                .toList();
    }
}
