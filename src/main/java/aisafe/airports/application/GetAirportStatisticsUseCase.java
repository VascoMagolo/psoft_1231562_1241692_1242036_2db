package aisafe.airports.application;

import aisafe.shared.application.UseCase;
import aisafe.airports.application.dtos.AirportStatisticsResponse;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.AirportStatisticsData;

import java.util.List;

@UseCase(readOnly = true)
public class GetAirportStatisticsUseCase {
    private final AirportRepository airportRepository;

    public GetAirportStatisticsUseCase(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public List<AirportStatisticsResponse> execute() {
        return airportRepository.findStatistics().stream()
                .map(d -> new AirportStatisticsResponse(d.iataCode(), d.name(), d.city(), d.country(), d.routeCount()))
                .toList();
    }
}
