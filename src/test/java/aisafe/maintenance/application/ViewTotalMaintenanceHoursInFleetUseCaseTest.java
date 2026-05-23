package aisafe.maintenance.application;

import aisafe.maintenance.application.dtos.ViewTotalMaintenanceHoursinFleetResponse;
import aisafe.maintenance.domain.MaintenanceRecord;
import aisafe.maintenance.domain.MaintenanceRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViewTotalMaintenanceHoursInFleetUseCaseTest {

    @Mock
    private MaintenanceRecordRepository repository;

    @InjectMocks
    private ViewTotalMaintenanceHoursInFleetUseCase viewTotalHours;

    @Test
    void ensureEmptyFleetReturnsZeroHours() {
        when(repository.findAll()).thenReturn(List.of());

        ViewTotalMaintenanceHoursinFleetResponse result = viewTotalHours.execute();

        assertEquals(0, result.totalHours());
    }

    @Test
    void ensureTotalHoursAreSummedCorrectly() {
        MaintenanceRecord r1 = mock(MaintenanceRecord.class);
        MaintenanceRecord r2 = mock(MaintenanceRecord.class);
        when(r1.getExpectedDuration()).thenReturn(4);
        when(r2.getExpectedDuration()).thenReturn(8);
        when(repository.findAll()).thenReturn(List.of(r1, r2));

        ViewTotalMaintenanceHoursinFleetResponse result = viewTotalHours.execute();

        assertEquals(12, result.totalHours());
    }
}
