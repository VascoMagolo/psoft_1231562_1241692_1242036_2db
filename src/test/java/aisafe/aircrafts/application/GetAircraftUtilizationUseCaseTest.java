package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.UtilizationDataPointResponse;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.routes.domain.ScheduledFlight;
import aisafe.routes.domain.ScheduledFlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class GetAircraftUtilizationUseCaseTest {

    private AircraftRepository aircraftRepository;
    private ScheduledFlightRepository scheduledFlightRepository;
    private GetAircraftUtilizationUseCase useCase;

    @BeforeEach
    void setUp() {
        aircraftRepository = Mockito.mock(AircraftRepository.class);
        scheduledFlightRepository = Mockito.mock(ScheduledFlightRepository.class);
        useCase = new GetAircraftUtilizationUseCase(aircraftRepository, scheduledFlightRepository);
    }

    @Test
    void execute_WhenAircraftDoesNotExist_ThrowsException() {
        when(aircraftRepository.existsByRegistrationNumber(any(RegistrationNumber.class))).thenReturn(false);

        assertThrows(AircraftNotFoundException.class, () -> 
                useCase.execute("XX-XXX", LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 2))
        );
    }

    @Test
    void execute_WhenValidRange_ReturnsDataPoints() {
        when(aircraftRepository.existsByRegistrationNumber(any(RegistrationNumber.class))).thenReturn(true);
        
        LocalDate start = LocalDate.of(2023, 1, 1);
        LocalDate end = LocalDate.of(2023, 1, 2);
        
        ScheduledFlight flight = Mockito.mock(ScheduledFlight.class);
        when(flight.getDepartureDateTime()).thenReturn(OffsetDateTime.of(2023, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC));
        when(flight.getArrivalDateTime()).thenReturn(OffsetDateTime.of(2023, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC));
        
        when(scheduledFlightRepository.findFlightsForUtilization(eq("XX-XXX"), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(List.of(flight));

        List<UtilizationDataPointResponse> result = useCase.execute("XX-XXX", start, end);

        assertEquals(2, result.size());
        assertEquals(start, result.get(0).date());
        assertEquals(2.0, result.get(0).flightHours());
        assertEquals(2.0 / 24.0 * 100.0, result.get(0).utilizationRatePercentage());
        
        assertEquals(end, result.get(1).date());
        assertEquals(0.0, result.get(1).flightHours());
        assertEquals(0.0, result.get(1).utilizationRatePercentage());
    }
}
