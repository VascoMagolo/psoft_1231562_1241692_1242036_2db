package aisafe.maintenance.application;

import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.maintenance.domain.MaintenanceRecordRepository;
import aisafe.shared.domain.PaginatedResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViewAllMaintenanceRecordsUseCaseTest {

    @Mock
    private MaintenanceRecordRepository repository;

    @Mock
    private AircraftRepository aircraftRepository;

    @InjectMocks
    private ViewAllMaintenanceRecordsUseCase viewAllMaintenanceRecords;

    @Test
    void ensureRecordsAreReturnedSuccessfully() {
        RegistrationNumber reg = new RegistrationNumber("CS-TPA");
        when(aircraftRepository.findByRegistrationNumber(reg)).thenReturn(Optional.of(mock(Aircraft.class)));
        when(repository.findByAircraftRegistration(eq("CS-TPA"), anyInt(), anyInt())).thenReturn(new PaginatedResult<>(List.of(), 0));

        assertDoesNotThrow(() -> viewAllMaintenanceRecords.execute(reg, 0, 20));
    }

    @Test
    void ensureExceptionWhenAircraftNotFound() {
        RegistrationNumber reg = new RegistrationNumber("CS-XXX");
        when(aircraftRepository.findByRegistrationNumber(reg)).thenReturn(Optional.empty());

        assertThrows(AircraftNotFoundException.class, () ->
                viewAllMaintenanceRecords.execute(reg, 0, 20));
        verify(repository, never()).findByAircraftRegistration(any(), anyInt(), anyInt());
    }
}
