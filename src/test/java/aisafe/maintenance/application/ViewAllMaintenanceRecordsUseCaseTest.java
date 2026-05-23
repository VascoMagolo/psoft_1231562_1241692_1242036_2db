package aisafe.maintenance.application;

import aisafe.aircrafts.domain.AircraftNotFoundException;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.maintenance.domain.MaintenanceRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        when(aircraftRepository.findByRegistrationNumber(reg)).thenReturn(Optional.of(mock(aisafe.aircrafts.domain.Aircraft.class)));
        when(repository.findByAircraftRegistrationNumber(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        assertDoesNotThrow(() -> viewAllMaintenanceRecords.execute(reg, Pageable.unpaged()));
    }

    @Test
    void ensureExceptionWhenAircraftNotFound() {
        RegistrationNumber reg = new RegistrationNumber("CS-XXX");
        when(aircraftRepository.findByRegistrationNumber(reg)).thenReturn(Optional.empty());

        assertThrows(AircraftNotFoundException.class, () ->
                viewAllMaintenanceRecords.execute(reg, Pageable.unpaged()));
        verify(repository, never()).findByAircraftRegistrationNumber(any(), any());
    }
}
