package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.TopUtilizedModelResponse;
import aisafe.flights.domain.ModelUtilizationData;
import aisafe.flights.domain.ScheduledFlightRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetTopUtilizedModelsUseCaseTest {

    @Mock
    private ScheduledFlightRepository repository;

    @InjectMocks
    private GetTopUtilizedModelsUseCase useCase;

    @Test
    void executeWithAssignmentsReturnsModels() {
        ModelUtilizationData data = new ModelUtilizationData("A320", 150L);

        when(repository.findTopModelsByAssignments(5)).thenReturn(List.of(data));

        List<TopUtilizedModelResponse> result = useCase.execute("ASSIGNMENTS");

        assertEquals(1, result.size());
        assertEquals("A320", result.get(0).modelName());
        assertEquals(150L, result.get(0).utilizationValue());
    }

    @Test
    void executeWithHoursReturnsModels() {
        ModelUtilizationData data = new ModelUtilizationData("A320", 5000L);

        when(repository.findTopModelsByFlightHours(5)).thenReturn(List.of(data));

        List<TopUtilizedModelResponse> result = useCase.execute("HOURS");

        assertEquals(1, result.size());
        assertEquals("A320", result.get(0).modelName());
        assertEquals(5000L, result.get(0).utilizationValue());
    }

    @Test
    void executeWithInvalidCriteriaThrowsException() {
        assertThrows(aisafe.shared.domain.DomainException.class, () -> useCase.execute("INVALID"));
    }
}
