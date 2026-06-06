package aisafe.airports.application;

import aisafe.airports.application.dtos.AirportResponse;
import aisafe.airports.application.dtos.UpdateAirportDetailsRequest;
import aisafe.airports.domain.Airport;
import aisafe.airports.domain.AirportNotFoundException;
import aisafe.airports.domain.AirportRepository;
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
class UpdateAirportDetailsUseCaseTest {

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private UpdateAirportDetailsUseCase updateAirportDetails;

    private Airport buildAirport(String iataCode) {
        return new Airport(iataCode, "Lisbon Airport", "Lisbon", "Portugal", "Europe",
                "Europe/Lisbon", 38.77, -9.13,
                List.of(new Runway("03/21", 3000, "030/210")));
    }

    @Test
    void ensureAirportDetailsAreUpdatedSuccessfully() {
        Airport airport = buildAirport("LIS");
        when(airportRepository.findByIataCodeCode("LIS")).thenReturn(Optional.of(airport));

        UpdateAirportDetailsRequest request = new UpdateAirportDetailsRequest(
                "06:00-23:00", null, null, null, null, null);

        AirportResponse result = updateAirportDetails.execute("LIS", request);

        assertNotNull(result);
        verify(airportRepository).save(any(Airport.class));
    }

    @Test
    void ensureExceptionWhenAirportNotFound() {
        when(airportRepository.findByIataCodeCode("XXX")).thenReturn(Optional.empty());

        UpdateAirportDetailsRequest request = new UpdateAirportDetailsRequest(
                "06:00-23:00", null, null, null, null, null);

        assertThrows(AirportNotFoundException.class, () -> updateAirportDetails.execute("XXX", request));
        verify(airportRepository, never()).save(any());
    }
}
