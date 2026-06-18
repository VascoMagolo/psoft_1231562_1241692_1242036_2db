package aisafe.flights.application;

import aisafe.flights.application.dtos.FlightUtilizationResponse;
import aisafe.flights.domain.InvalidFlightDateRangeException;
import aisafe.flights.domain.RouteUtilizationData;
import aisafe.flights.domain.ScheduledFlightRepository;
import aisafe.shared.application.UseCase;
import aisafe.shared.domain.PaginatedResult;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@UseCase(readOnly = true)
public class GenerateFlightUtilizationReportUseCase {

    private final ScheduledFlightRepository repository;

    public GenerateFlightUtilizationReportUseCase(ScheduledFlightRepository repository) {
        this.repository = repository;
    }

    public PaginatedResult<FlightUtilizationResponse> execute(OffsetDateTime startDate, OffsetDateTime endDate, Integer page, Integer size) {
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new InvalidFlightDateRangeException("startDate cannot be after endDate");
        }

        PaginatedResult<RouteUtilizationData> result = repository.getFlightUtilizationReport(startDate, endDate, pageNumber, pageSize);
        
        List<FlightUtilizationResponse> mappedData = result.data().stream()
                .map(data -> new FlightUtilizationResponse(
                        data.routeId(),
                        data.origin(),
                        data.destination(),
                        data.count()
                ))
                .collect(Collectors.toList());

        return new PaginatedResult<>(mappedData, result.totalElements());
    }
}
