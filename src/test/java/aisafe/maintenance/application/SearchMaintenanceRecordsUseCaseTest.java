package aisafe.maintenance.application;

import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.maintenance.application.dtos.MaintenanceRecordResponse;
import aisafe.maintenance.domain.MaintenanceComponent;
import aisafe.maintenance.domain.MaintenanceRecordRepository;
import aisafe.shared.domain.PaginatedResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchMaintenanceRecordsUseCaseTest {

    @Mock
    private MaintenanceRecordRepository repository;

    @InjectMocks
    private SearchMaintenanceRecordsUseCase searchMaintenanceRecords;

    @Test
    void ensureReturnsEmptyPageWhenNoRecordsMatch() {
        when(repository.search(isNull(), isNull(), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(new PaginatedResult<>(List.of(), 0));

        PaginatedResult<MaintenanceRecordResponse> result = searchMaintenanceRecords.execute(null, null, null, null, 0, 20);

        assertEquals(0, result.totalElements());
        assertEquals(0, result.data().size());
    }

    @Test
    void ensureFiltersArePassedToRepositoryForAircraftRegistration() {
        RegistrationNumber reg = new RegistrationNumber("CS-TPA");
        when(repository.search(eq(reg), isNull(), isNull(), isNull(), anyInt(), anyInt()))
                .thenReturn(new PaginatedResult<>(List.of(), 0));

        searchMaintenanceRecords.execute(reg, null, null, null, 0, 20);

        verify(repository).search(eq(reg), isNull(), isNull(), isNull(), eq(0), eq(20));
    }

    @Test
    void ensureFiltersArePassedToRepositoryForDateRange() {
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 12, 31, 23, 59);
        when(repository.search(isNull(), eq(from), eq(to), isNull(), anyInt(), anyInt()))
                .thenReturn(new PaginatedResult<>(List.of(), 0));

        searchMaintenanceRecords.execute(null, from, to, null, 0, 20);

        verify(repository).search(isNull(), eq(from), eq(to), isNull(), eq(0), eq(20));
    }

    @Test
    void ensureFiltersArePassedToRepositoryForComponent() {
        when(repository.search(isNull(), isNull(), isNull(), eq(MaintenanceComponent.ENGINE), anyInt(), anyInt()))
                .thenReturn(new PaginatedResult<>(List.of(), 0));

        searchMaintenanceRecords.execute(null, null, null, MaintenanceComponent.ENGINE, 0, 20);

        verify(repository).search(isNull(), isNull(), isNull(), eq(MaintenanceComponent.ENGINE), eq(0), eq(20));
    }

    @Test
    void ensureAllFiltersArePassedToRepositoryTogether() {
        RegistrationNumber reg = new RegistrationNumber("CS-TPA");
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 6, 30, 23, 59);
        when(repository.search(eq(reg), eq(from), eq(to), eq(MaintenanceComponent.AVIONICS), anyInt(), anyInt()))
                .thenReturn(new PaginatedResult<>(List.of(), 0));

        searchMaintenanceRecords.execute(reg, from, to, MaintenanceComponent.AVIONICS, 0, 10);

        verify(repository).search(eq(reg), eq(from), eq(to), eq(MaintenanceComponent.AVIONICS), eq(0), eq(10));
    }
}
