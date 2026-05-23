package aisafe.airports.application;

import aisafe.DuplicateResourceException;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.aircrafts.domain.AircraftNotFoundException;
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
        AircraftModel model = buildModel();
        when(airportRepository.findByIataCodeCode("LIS")).thenReturn(Optional.of(airport));
        when(aircraftModelRepository.findById(1L)).thenReturn(Optional.of(model));
        when(certificationRepository.existsByAirportAndAircraftModelId(airport, 1L)).thenReturn(false);
        when(certificationRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        assertDoesNotThrow(() -> addCertification.execute("LIS", new AddCertificationRequest(1L)));
        verify(certificationRepository).save(any(AircraftCertification.class));
    }

    @Test
    void ensureExceptionWhenAirportNotFound() {
        when(airportRepository.findByIataCodeCode("XYZ")).thenReturn(Optional.empty());

        assertThrows(AirportNotFoundException.class, () -> addCertification.execute("XYZ", new AddCertificationRequest(1L)));
        verify(certificationRepository, never()).save(any());
    }

    @Test
    void ensureExceptionWhenModelNotFound() {
        Airport airport = buildAirport();
        when(airportRepository.findByIataCodeCode("LIS")).thenReturn(Optional.of(airport));
        when(aircraftModelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AircraftNotFoundException.class, () -> addCertification.execute("LIS", new AddCertificationRequest(99L)));
        verify(certificationRepository, never()).save(any());
    }

    @Test
    void ensureExceptionWhenCertificationAlreadyExists() {
        Airport airport = buildAirport();
        AircraftModel model = buildModel();
        when(airportRepository.findByIataCodeCode("LIS")).thenReturn(Optional.of(airport));
        when(aircraftModelRepository.findById(1L)).thenReturn(Optional.of(model));
        when(certificationRepository.existsByAirportAndAircraftModelId(airport, 1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> addCertification.execute("LIS", new AddCertificationRequest(1L)));
        verify(certificationRepository, never()).save(any());
    }
}
