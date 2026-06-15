package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.TopUtilizedModelResponse;
import aisafe.aircrafts.infrastructure.persistence.jpa.TopUtilizedModelProjection;
import aisafe.routes.infrastructure.persistence.jpa.SpringDataScheduledFlightRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetTopUtilizedModelsUseCaseTest {

    @Mock
    private SpringDataScheduledFlightRepository repository;

    @InjectMocks
    private GetTopUtilizedModelsUseCase useCase;

    @Test
    void executeWithAssignmentsReturnsModels() {
        TopUtilizedModelProjection projection = mock(TopUtilizedModelProjection.class);
        when(projection.getModelName()).thenReturn("A320");
        when(projection.getUtilizationValue()).thenReturn(150L);

        when(repository.findTopModelsByAssignments(any(Pageable.class))).thenReturn(List.of(projection));

        List<TopUtilizedModelResponse> result = useCase.execute("ASSIGNMENTS");

        assertEquals(1, result.size());
        assertEquals("A320", result.get(0).modelName());
        assertEquals(150L, result.get(0).utilizationValue());
    }

    @Test
    void executeWithHoursReturnsModels() {
        TopUtilizedModelProjection projection = mock(TopUtilizedModelProjection.class);
        when(projection.getModelName()).thenReturn("A320");
        when(projection.getUtilizationValue()).thenReturn(5000L);

        when(repository.findTopModelsByFlightHours(any(Pageable.class))).thenReturn(List.of(projection));

        List<TopUtilizedModelResponse> result = useCase.execute("HOURS");

        assertEquals(1, result.size());
        assertEquals("A320", result.get(0).modelName());
        assertEquals(5000L, result.get(0).utilizationValue());
    }

    @Test
    void executeWithInvalidCriteriaThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute("INVALID"));
    }
}
