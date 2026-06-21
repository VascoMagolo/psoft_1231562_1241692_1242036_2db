package aisafe.flights.application;

import aisafe.flights.application.dtos.FlightUtilizationResponse;
import aisafe.flights.domain.InvalidFlightDateRangeException;
import aisafe.flights.domain.RouteUtilizationData;
import aisafe.flights.domain.ScheduledFlightRepository;
import aisafe.shared.domain.PaginatedResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateFlightUtilizationReportUseCaseTest {

    @Mock
    private ScheduledFlightRepository repository;

    private GenerateFlightUtilizationReportUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GenerateFlightUtilizationReportUseCase(repository);
    }

    @Test
    void ensureReportIsGenerated() {
        OffsetDateTime start = OffsetDateTime.now().minusDays(10);
        OffsetDateTime end = OffsetDateTime.now();
        List<RouteUtilizationData> mockData = List.of(new RouteUtilizationData(1L, "OPO", "LIS", 5L));
        PaginatedResult<RouteUtilizationData> mockPaginatedResult = new PaginatedResult<>(mockData, 1L);

        when(repository.getFlightUtilizationReport(start, end, 0, 20)).thenReturn(mockPaginatedResult);

        PaginatedResult<FlightUtilizationResponse> result = useCase.execute(start, end, 0, 20);

        assertEquals(1L, result.totalElements());
        assertEquals(1, result.data().size());
        FlightUtilizationResponse mapped = result.data().get(0);
        assertEquals(1L, mapped.routeId());
        assertEquals("OPO", mapped.origin());
        assertEquals("LIS", mapped.destination());
        assertEquals(5L, mapped.count());
    }

    @Test
    void ensureFailsWhenStartDateAfterEndDate() {
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = start.minusDays(1);

        assertThrows(InvalidFlightDateRangeException.class, () -> useCase.execute(start, end, 0, 20));
    }

    @Test
    void ensureReportIsGeneratedWithDefaultPageAndSize() {
        OffsetDateTime start = OffsetDateTime.now().minusDays(10);
        OffsetDateTime end = OffsetDateTime.now();
        List<RouteUtilizationData> mockData = List.of(new RouteUtilizationData(1L, "OPO", "LIS", 5L));
        PaginatedResult<RouteUtilizationData> mockPaginatedResult = new PaginatedResult<>(mockData, 1L);

        when(repository.getFlightUtilizationReport(start, end, 0, 20)).thenReturn(mockPaginatedResult);

        PaginatedResult<FlightUtilizationResponse> result = useCase.execute(start, end, null, null);

        assertEquals(1L, result.totalElements());
    }

    @Test
    void ensureReportIsGeneratedWithNullDates() {
        List<RouteUtilizationData> mockData = List.of(new RouteUtilizationData(1L, "OPO", "LIS", 5L));
        PaginatedResult<RouteUtilizationData> mockPaginatedResult = new PaginatedResult<>(mockData, 1L);

        when(repository.getFlightUtilizationReport(null, null, 0, 20)).thenReturn(mockPaginatedResult);

        PaginatedResult<FlightUtilizationResponse> result = useCase.execute(null, null, 0, 20);

        assertEquals(1L, result.totalElements());
     }

    @Test
    void ensureReportIsGeneratedWithNullEndDate() {
        OffsetDateTime start = OffsetDateTime.now().minusDays(10);
        List<RouteUtilizationData> mockData = List.of(new RouteUtilizationData(1L, "OPO", "LIS", 5L));
        PaginatedResult<RouteUtilizationData> mockPaginatedResult = new PaginatedResult<>(mockData, 1L);

        when(repository.getFlightUtilizationReport(start, null, 0, 20)).thenReturn(mockPaginatedResult);

        PaginatedResult<FlightUtilizationResponse> result = useCase.execute(start, null, 0, 20);

        assertEquals(1L, result.totalElements());
    }

    @Test
    void ensureReportIsGeneratedWithNullStartDate() {
        OffsetDateTime end = OffsetDateTime.now();
        List<RouteUtilizationData> mockData = List.of(new RouteUtilizationData(1L, "OPO", "LIS", 5L));
        PaginatedResult<RouteUtilizationData> mockPaginatedResult = new PaginatedResult<>(mockData, 1L);

        when(repository.getFlightUtilizationReport(null, end, 0, 20)).thenReturn(mockPaginatedResult);

        PaginatedResult<FlightUtilizationResponse> result = useCase.execute(null, end, 0, 20);

        assertEquals(1L, result.totalElements());
    }
}
