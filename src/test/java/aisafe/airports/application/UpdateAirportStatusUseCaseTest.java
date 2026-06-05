package aisafe.airports.application;

import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
import aisafe.airports.domain.AirportStatus;
import aisafe.airports.domain.Runway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateAirportStatusUseCaseTest {

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private UpdateAirportStatusUseCase updateAirportStatus;

    private Airport buildAirport() {
        return new Airport("LIS", "Lisbon Airport", "Lisbon", "Portugal", "Europe", "Europe/Lisbon",
                38.77, -9.13, List.of(new Runway("03/21", 3000, "030/210")));
    }

    @Test
    void ensureStatusIsUpdatedSuccessfully() {
        Airport airport = buildAirport();
        when(airportRepository.findByIataCodeCode("LIS")).thenReturn(Optional.of(airport));

        assertDoesNotThrow(() -> updateAirportStatus.execute("LIS", AirportStatus.CLOSED));
        verify(airportRepository).save(airport);
    }

    @Test
    void ensureExceptionWhenAirportNotFound() {
        when(airportRepository.findByIataCodeCode("XYZ")).thenReturn(Optional.empty());

        assertThrows(AirportNotFoundException.class, () -> updateAirportStatus.execute("XYZ", AirportStatus.CLOSED));
        verify(airportRepository, never()).save(any());
    }
}
