package aisafe.maintenance.application;

import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.maintenance.application.dtos.MaintenanceCostByAircraftResponse;
import aisafe.maintenance.domain.MaintenanceRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewMaintenanceCostByAircraftUseCaseTest {

    @Mock
    private MaintenanceRecordRepository recordRepository;

    @Mock
    private AircraftRepository aircraftRepository;

    @InjectMocks
    private ViewMaintenanceCostByAircraftUseCase useCase;

    @Test
    void ensureCostIsReturnedForKnownAircraft() {
        when(aircraftRepository.existsByRegistrationNumber(any(RegistrationNumber.class))).thenReturn(true);
        when(recordRepository.sumCostByAircraftRegistration(any(RegistrationNumber.class)))
                .thenReturn(BigDecimal.valueOf(1500));

        MaintenanceCostByAircraftResponse response = useCase.execute("CS-TPA");

        assertNotNull(response);
        assertEquals("CS-TPA", response.aircraftRegistration());
        assertEquals(BigDecimal.valueOf(1500), response.totalCost());
    }

    @Test
    void ensureZeroCostWhenNoRecordsExist() {
        when(aircraftRepository.existsByRegistrationNumber(any(RegistrationNumber.class))).thenReturn(true);
        when(recordRepository.sumCostByAircraftRegistration(any(RegistrationNumber.class)))
                .thenReturn(BigDecimal.ZERO);

        MaintenanceCostByAircraftResponse response = useCase.execute("CS-TPA");

        assertEquals(BigDecimal.ZERO, response.totalCost());
    }

    @Test
    void ensureExceptionWhenAircraftNotFound() {
        when(aircraftRepository.existsByRegistrationNumber(any(RegistrationNumber.class))).thenReturn(false);

        assertThrows(AircraftNotFoundException.class, () -> useCase.execute("CS-TPA"));
    }
}
