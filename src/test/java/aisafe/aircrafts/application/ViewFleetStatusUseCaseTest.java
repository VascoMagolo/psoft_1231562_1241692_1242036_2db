package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.FleetStatusAircraftResponse;
import aisafe.aircrafts.application.dtos.FleetStatusGroupResponse;
import aisafe.aircrafts.application.dtos.FleetStatusResponse;
import aisafe.aircrafts.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewFleetStatusUseCaseTest {

    @Mock
    private AircraftRepository aircraftRepository;

    @InjectMocks
    private ViewFleetStatusUseCase viewFleetStatusUseCase;

    private Aircraft createAircraft(String registration, String modelName, AircraftStatus status) {
        AircraftModel model = new AircraftModel(modelName, Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "img.jpg", 180);
        return new Aircraft(status, LocalDate.of(2020, 1, 1), model, new RegistrationNumber(registration), 150, 5000.0, List.of());
    }

    @Test
    void ensureFleetStatusGroupsAircraftByStatus() {
        List<Aircraft> fleet = List.of(
                createAircraft("CS-TPA", "A320", AircraftStatus.AVAILABLE),
                createAircraft("CS-TKA", "B737", AircraftStatus.UNDER_MAINTENANCE),
                createAircraft("CS-TKB", "A320", AircraftStatus.UNDER_MAINTENANCE),
                createAircraft("CS-TNE", "E195", AircraftStatus.IN_FLIGHT)
        );
        when(aircraftRepository.findAll()).thenReturn(fleet);

        FleetStatusResponse response = viewFleetStatusUseCase.execute();

        assertEquals(4, response.totalAircraft());
        assertEquals(4, response.statusGroups().data().size());

        FleetStatusGroupResponse available = findGroup(response, AircraftStatus.AVAILABLE);
        assertEquals(1, available.aircrafts().totalElements());
        assertEquals("CS-TPA", available.aircrafts().data().get(0).registrationNumber());

        FleetStatusGroupResponse underMaintenance = findGroup(response, AircraftStatus.UNDER_MAINTENANCE);
        assertEquals(2, underMaintenance.aircrafts().totalElements());

        FleetStatusGroupResponse inFlight = findGroup(response, AircraftStatus.IN_FLIGHT);
        assertEquals(1, inFlight.aircrafts().totalElements());

        FleetStatusGroupResponse inactive = findGroup(response, AircraftStatus.INACTIVE);
        assertEquals(0, inactive.aircrafts().totalElements());
        assertTrue(inactive.aircrafts().data().isEmpty());
    }

    @Test
    void ensureFleetStatusReturnsAllGroupsWhenFleetIsEmpty() {
        when(aircraftRepository.findAll()).thenReturn(List.of());

        FleetStatusResponse response = viewFleetStatusUseCase.execute();

        assertEquals(0, response.totalAircraft());
        assertEquals(4, response.statusGroups().data().size());
        for (FleetStatusGroupResponse group : response.statusGroups().data()) {
            assertEquals(0, group.aircrafts().totalElements());
            assertTrue(group.aircrafts().data().isEmpty());
        }
    }

    @Test
    void ensureFleetStatusAircraftDtoContainsCorrectFields() {
        Aircraft aircraft = createAircraft("CS-TPA", "A320", AircraftStatus.AVAILABLE);
        when(aircraftRepository.findAll()).thenReturn(List.of(aircraft));

        FleetStatusResponse response = viewFleetStatusUseCase.execute();

        FleetStatusGroupResponse available = findGroup(response, AircraftStatus.AVAILABLE);
        FleetStatusAircraftResponse dto = available.aircrafts().data().get(0);
        assertEquals("CS-TPA", dto.registrationNumber());
        assertEquals("A320", dto.model());
        assertEquals(Manufacturer.AIRBUS, dto.manufacturer());
    }

    private FleetStatusGroupResponse findGroup(FleetStatusResponse response, AircraftStatus status) {
        return response.statusGroups().data().stream()
                .filter(g -> g.status() == status)
                .findFirst()
                .orElseThrow();
    }
}
