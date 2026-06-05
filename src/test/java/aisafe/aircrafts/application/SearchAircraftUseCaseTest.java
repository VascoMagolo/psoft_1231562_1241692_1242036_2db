package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.SearchAircraftUseCaseResponse;
import aisafe.aircrafts.domain.*;
import aisafe.shared.application.dtos.PaginatedResult;
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
class SearchAircraftUseCaseTest {

    @Mock
    private AircraftRepository aircraftRepository;

    @InjectMocks
    private SearchAircraftUseCase searchAircraftUseCase;

    private Aircraft aircraft;

    @BeforeEach
    void setUp() {
        AircraftModel model = new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 180);
        aircraft = new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1), model, new RegistrationNumber("CS-TPA"), 150, List.of());
    }

    @Test
    void ensureSearchReturnsResultsSuccessfully() {
        PaginatedResult<Aircraft> domainResult = new PaginatedResult<>(List.of(aircraft), 1L);
        when(aircraftRepository.searchAircrafts("A320", AircraftStatus.AVAILABLE, 2020, 0, 10)).thenReturn(domainResult);

        PaginatedResult<SearchAircraftUseCaseResponse> result = searchAircraftUseCase.execute("A320", "AVAILABLE", 2020, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.data().size());
        assertEquals("CS-TPA", result.data().get(0).registrationNumber());
    }

    @Test
    void ensureExceptionForInvalidStatus() {
        assertThrows(AircraftInvalidFieldException.class, () -> searchAircraftUseCase.execute(null, "INVALID_STATUS", null, 0, 10));
    }
}
