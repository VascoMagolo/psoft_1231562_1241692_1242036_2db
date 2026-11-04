package aisafe.airports.application;

import aisafe.shared.application.UseCase;
import aisafe.airports.application.dtos.AirportGroupResponse;
import aisafe.airports.application.dtos.AirportGroupSummaryResponse;
import aisafe.airports.domain.AirportGroupingData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@UseCase(readOnly = true)
public class ListAirportsByRegionUseCase {
    private final AirportListings airports;

    public ListAirportsByRegionUseCase(AirportListings airports) {
        this.airports = airports;
    }

    public List<AirportGroupResponse> execute(String groupBy) {
        boolean byCountry = "country".equalsIgnoreCase(groupBy);
        List<AirportGroupingData> rows = byCountry
                ? airports.groupedByCountry()
                : airports.groupedByRegion();

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
