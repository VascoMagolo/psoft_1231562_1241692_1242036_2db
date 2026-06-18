package aisafe.maintenance.application;

import aisafe.aircrafts.domain.*;
import aisafe.maintenance.application.dtos.AverageTurnaroundByModelResponse;
import aisafe.maintenance.domain.MaintenanceRecordRepository;
import aisafe.maintenance.domain.MaintenanceTurnaroundData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewAverageMaintenanceTurnaroundUseCaseTest {

    @Mock
    private MaintenanceRecordRepository recordRepository;

    @Mock
    private AircraftRepository aircraftRepository;

    @Mock
    private AircraftModelRepository aircraftModelRepository;

    @InjectMocks
    private ViewAverageMaintenanceTurnaroundUseCase useCase;

    private Aircraft buildAircraft(String reg, AircraftModel model) {
        return new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1),
                model, new RegistrationNumber(reg), 180, 2000.0, List.of());
    }

    @Test
    void ensureAverageHoursAreReturnedForModel() {
        AircraftModel model = new AircraftModel("A320", Manufacturer.AIRBUS, 1000.0, 5000.0, 800.0, "img.jpg", 180);
        when(aircraftModelRepository.existsByModelName("A320")).thenReturn(true);
        when(aircraftRepository.findAll()).thenReturn(List.of(
                buildAircraft("CS-TPA", model),
                buildAircraft("CS-TPB", model)
        ));
        when(recordRepository.findAverageTurnaroundByRegistrations(anyString(), anyList()))
                .thenReturn(new MaintenanceTurnaroundData("A320", 32.0));

        AverageTurnaroundByModelResponse response = useCase.execute("A320");

        assertNotNull(response);
        assertEquals("A320", response.modelName());
        assertEquals(32.0, response.averageHours());
    }

    @Test
    void ensureZeroAverageWhenNoAircraftOfModelExist() {
        AircraftModel otherModel = new AircraftModel("B737", Manufacturer.BOEING, 800.0, 4000.0, 700.0, "img.jpg", 160);
        when(aircraftModelRepository.existsByModelName("A320")).thenReturn(true);
        when(aircraftRepository.findAll()).thenReturn(List.of(buildAircraft("CS-TPA", otherModel)));
        when(recordRepository.findAverageTurnaroundByRegistrations(anyString(), anyList()))
                .thenReturn(new MaintenanceTurnaroundData("A320", 0.0));

        AverageTurnaroundByModelResponse response = useCase.execute("A320");

        assertEquals(0.0, response.averageHours());
    }

    @Test
    void ensureExceptionWhenModelNotFound() {
        when(aircraftModelRepository.existsByModelName(anyString())).thenReturn(false);

        assertThrows(AircraftModelNotFoundException.class, () -> useCase.execute("UNKNOWN"));
    }
}
