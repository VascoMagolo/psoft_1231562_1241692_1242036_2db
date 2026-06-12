package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.FuelEfficiencyResponse;
import aisafe.aircrafts.domain.*;
import aisafe.routes.domain.Route;
import aisafe.routes.domain.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculateFuelEfficiencyUseCaseTest {

    @Mock
    private AircraftRepository aircraftRepository;

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private CalculateFuelEfficiencyUseCase useCase;

    private Aircraft aircraft;
    private Route route;

    @BeforeEach
    void setUp() {
        AircraftModel model = new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 180);
        aircraft = new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1), model, new RegistrationNumber("CS-TPA"), 150, 5000.0, List.of());
        route = new Route("OPO", "LIS", 45, 500.0, 100);
        route.setId(1L);
    }

    @Test
    void ensureCalculatesOnlyPerAircraftWhenRouteNotProvided() {
        when(aircraftRepository.findByRegistrationNumber(any(RegistrationNumber.class))).thenReturn(Optional.of(aircraft));

        FuelEfficiencyResponse response = useCase.execute("CS-TPA", null);

        assertNotNull(response);
        assertEquals("CS-TPA", response.registrationNumber());
        assertEquals(26730.0 / 5000.0, response.fuelConsumptionPerDistanceUnit());
        assertNull(response.routeId());
        assertNull(response.fuelNeededForRoute());
        verify(routeRepository, never()).findById(any());
    }

    @Test
    void ensureCalculatesPerAircraftAndRouteWhenProvided() {
        when(aircraftRepository.findByRegistrationNumber(any(RegistrationNumber.class))).thenReturn(Optional.of(aircraft));
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));

        FuelEfficiencyResponse response = useCase.execute("CS-TPA", 1L);

        assertNotNull(response);
        assertEquals("CS-TPA", response.registrationNumber());
        assertEquals(26730.0 / 5000.0, response.fuelConsumptionPerDistanceUnit());
        assertEquals(1L, response.routeId());
        assertEquals((26730.0 / 5000.0) * 500.0, response.fuelNeededForRoute());
    }

    @Test
    void ensureExceptionWhenAircraftNotFound() {
        when(aircraftRepository.findByRegistrationNumber(any(RegistrationNumber.class))).thenReturn(Optional.empty());

        assertThrows(AircraftNotFoundException.class, () -> useCase.execute("CS-TPA", null));
    }

    @Test
    void ensureExceptionWhenRouteNotFound() {
        when(aircraftRepository.findByRegistrationNumber(any(RegistrationNumber.class))).thenReturn(Optional.of(aircraft));
        when(routeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> useCase.execute("CS-TPA", 1L));
    }
}