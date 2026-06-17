package aisafe.maintenance.application;

import aisafe.maintenance.application.dtos.MaintenancePartResponse;
import aisafe.maintenance.domain.MaintenanceComponent;
import aisafe.maintenance.domain.MaintenancePart;
import aisafe.maintenance.domain.MaintenancePartRepository;
import aisafe.shared.domain.PaginatedResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchMaintenancePartUseCaseTest {

    @Mock
    private MaintenancePartRepository repository;

    @InjectMocks
    private SearchMaintenancePartUseCase searchUseCase;

    private MaintenancePart samplePart;

    @BeforeEach
    void setUp() {
        samplePart = new MaintenancePart("P001", "Engine Filter", "Desc", 10, 5, MaintenanceComponent.ENGINE);
    }

    @Test
    void ensureSearchCallsRepositoryAndReturnsDtos() {
        // Arrange
        when(repository.searchParts(anyString(), anyString(), any(), anyBoolean(), anyInt(), anyInt()))
                .thenReturn(new PaginatedResult<>(List.of(samplePart), 1));

        // Act
        PaginatedResult<MaintenancePartResponse> result = searchUseCase.execute(
                "P001", "Engine", MaintenanceComponent.ENGINE, false, 0, 10);

        // Assert
        assertEquals(1, result.data().size());
        assertEquals("P001", result.data().get(0).partNumber());
        assertEquals("Engine Filter", result.data().get(0).name());
        verify(repository).searchParts(eq("P001"), eq("Engine"), eq(MaintenanceComponent.ENGINE), eq(false), eq(0), eq(10));
    }

    @Test
    void ensureSearchWithLowStockOnlyCallsRepositoryCorrectly() {
        // Arrange
        when(repository.searchParts(isNull(), isNull(), isNull(), eq(true), anyInt(), anyInt()))
                .thenReturn(new PaginatedResult<>(List.of(samplePart), 1));

        // Act
        PaginatedResult<MaintenancePartResponse> result = searchUseCase.execute(
                null, null, null, true, 0, 10);

        // Assert
        assertEquals(1, result.data().size());
        verify(repository).searchParts(isNull(), isNull(), isNull(), eq(true), eq(0), eq(10));
    }
}
