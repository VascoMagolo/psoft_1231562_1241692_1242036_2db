package aisafe.airports.application;

import aisafe.shared.domain.DuplicateResourceException;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.aircrafts.domain.AircraftModelNotFoundException;
import aisafe.aircrafts.domain.Manufacturer;
import aisafe.airports.application.dtos.AddCertificationRequest;
import aisafe.airports.domain.*;
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
class AddAirportCertificationUseCaseTest {

    @Mock
    private AirportRepository airportRepository;

    @Mock
    private AircraftCertificationRepository certificationRepository;

    @Mock
    private AircraftModelRepository aircraftModelRepository;

    @InjectMocks
    private AddAirportCertificationUseCase addCertification;

    private Airport buildAirport() {
        return new Airport("LIS", "Lisbon Airport", "Lisbon", "Portugal", "Europe", "Europe/Lisbon",
                38.77, -9.13, List.of(new Runway("03/21", 3000, "030/210")));
    }

    private AircraftModel buildModel() {
        return new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 180);
    }

    @Test
    void ensureCertificationIsAddedSuccessfully() {
        Airport airport = buildAirport();
        when(airportRepository.findByIataCodeCode("LIS")).thenReturn(Optional.of(airport));
        when(aircraftModelRepository.existsByModelName("A320")).thenReturn(true);
        when(certificationRepository.existsByAirportAndAircraftModelName(airport, "A320")).thenReturn(false);
        when(certificationRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        assertDoesNotThrow(() -> addCertification.execute("LIS", new AddCertificationRequest("LIS", "A320")));
        verify(certificationRepository).save(any(AircraftCertification.class));
    }

    @Test
    void ensureExceptionWhenAirportNotFound() {
        when(airportRepository.findByIataCodeCode("XYZ")).thenReturn(Optional.empty());

        assertThrows(AirportNotFoundException.class, () -> addCertification.execute("XYZ", new AddCertificationRequest("XYZ", "A320")));
        verify(certificationRepository, never()).save(any());
    }

    @Test
    void ensureExceptionWhenModelNotFound() {
        Airport airport = buildAirport();
        when(airportRepository.findByIataCodeCode("LIS")).thenReturn(Optional.of(airport));
        when(aircraftModelRepository.existsByModelName("NON-EXISTENT")).thenReturn(false);

        assertThrows(AircraftModelNotFoundException.class, () -> addCertification.execute("LIS", new AddCertificationRequest("LIS", "NON-EXISTENT")));
        verify(certificationRepository, never()).save(any());
    }

    @Test
    void ensureExceptionWhenCertificationAlreadyExists() {
        Airport airport = buildAirport();
        when(airportRepository.findByIataCodeCode("LIS")).thenReturn(Optional.of(airport));
        when(aircraftModelRepository.existsByModelName("A320")).thenReturn(true);
        when(certificationRepository.existsByAirportAndAircraftModelName(airport, "A320")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> addCertification.execute("LIS", new AddCertificationRequest("LIS", "A320")));
        verify(certificationRepository, never()).save(any());
    }
}
