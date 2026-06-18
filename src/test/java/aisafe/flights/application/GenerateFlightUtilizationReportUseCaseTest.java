package aisafe.flights.application;

import aisafe.flights.application.dtos.RouteUtilizationResponse;
import aisafe.flights.domain.InvalidFlightDateRangeException;
import aisafe.flights.domain.RouteUtilizationData;
import aisafe.flights.domain.ScheduledFlightRepository;
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
        List<RouteUtilizationResponse> expected = List.of(new RouteUtilizationResponse(1L, "OPO", "LIS", 5L));

        when(repository.getFlightUtilizationReport(start, end, 0, 20)).thenReturn(mockData);

        List<RouteUtilizationResponse> result = useCase.execute(start, end, 0, 20);

        assertEquals(expected, result);
    }

    @Test
    void ensureFailsWhenStartDateAfterEndDate() {
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = start.minusDays(1);

        assertThrows(InvalidFlightDateRangeException.class, () -> useCase.execute(start, end, 0, 20));
    }
}
