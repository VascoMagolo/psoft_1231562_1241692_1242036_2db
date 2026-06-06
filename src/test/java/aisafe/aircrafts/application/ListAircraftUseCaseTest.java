package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.ListAircraftsUseCaseResponse;
import aisafe.aircrafts.domain.*;
import aisafe.shared.domain.PaginatedResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListAircraftUseCaseTest {

    @Mock
    private AircraftRepository aircraftRepository;

    @InjectMocks
    private ListAircraftUseCase listAircraftUseCase;

    private Aircraft aircraft;

    @BeforeEach
    void setUp() {
        AircraftModel model = new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 180);
        aircraft = new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1), model, new RegistrationNumber("CS-TPA"), 150, List.of());
    }

    @Test
    void ensureListReturnsResultsSuccessfully() {
        PaginatedResult<Aircraft> domainResult = new PaginatedResult<>(List.of(aircraft), 1L);
        when(aircraftRepository.findAll(0, 10)).thenReturn(domainResult);

        PaginatedResult<ListAircraftsUseCaseResponse> result = listAircraftUseCase.execute(0, 10);

        assertNotNull(result);
        assertEquals(1, result.data().size());
        assertEquals("CS-TPA", result.data().get(0).registrationNumber());
    }
}
