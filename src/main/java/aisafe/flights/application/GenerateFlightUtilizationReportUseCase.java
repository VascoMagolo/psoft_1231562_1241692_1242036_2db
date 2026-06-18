package aisafe.flights.application;

import aisafe.flights.application.dtos.RouteUtilizationResponse;
import aisafe.flights.domain.InvalidFlightDateRangeException;
import aisafe.flights.domain.ScheduledFlightRepository;
import aisafe.shared.application.UseCase;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@UseCase(readOnly = true)
public class GenerateFlightUtilizationReportUseCase {

    private final ScheduledFlightRepository repository;

    public GenerateFlightUtilizationReportUseCase(ScheduledFlightRepository repository) {
        this.repository = repository;
    }

    public List<RouteUtilizationResponse> execute(OffsetDateTime startDate, OffsetDateTime endDate, Integer page, Integer size) {
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new InvalidFlightDateRangeException("startDate cannot be after endDate");
        }

        return repository.getFlightUtilizationReport(startDate, endDate, pageNumber, pageSize).stream()
                .map(data -> new RouteUtilizationResponse(
                        data.routeId(),
                        data.origin(),
                        data.destination(),
                        data.count()
                ))
                .collect(Collectors.toList());
    }
}
