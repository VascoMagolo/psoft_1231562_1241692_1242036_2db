package aisafe.maintenance.application;

import aisafe.maintenance.application.dtos.ViewTotalMaintenanceHoursInFleetResponse;
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
        when(repository.sumTotalMaintenanceHours()).thenReturn(0L);

        ViewTotalMaintenanceHoursInFleetResponse result = viewTotalHours.execute();

        assertEquals(0, result.totalHours());
    }

    @Test
    void ensureTotalHoursAreSummedCorrectly() {
        when(repository.sumTotalMaintenanceHours()).thenReturn(12L);

        ViewTotalMaintenanceHoursInFleetResponse result = viewTotalHours.execute();

        assertEquals(12, result.totalHours());
    }
}
