package aisafe.flights.application;

import aisafe.flights.domain.RouteUtilizationData;
import aisafe.flights.domain.ScheduledFlightRepository;
import aisafe.shared.application.UseCase;

import java.time.OffsetDateTime;
import java.util.List;

@UseCase(readOnly = true)
public class GenerateFlightUtilizationReportUseCase {

    private final ScheduledFlightRepository repository;

    public GenerateFlightUtilizationReportUseCase(ScheduledFlightRepository repository) {
        this.repository = repository;
    }

    public List<RouteUtilizationData> execute(OffsetDateTime startDate, OffsetDateTime endDate, Integer page, Integer size) {
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate cannot be after endDate");
        }

        return repository.getFlightUtilizationReport(startDate, endDate, pageNumber, pageSize);
    }
}
