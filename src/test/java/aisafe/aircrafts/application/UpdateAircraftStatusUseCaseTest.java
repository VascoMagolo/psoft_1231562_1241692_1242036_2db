package aisafe.aircrafts.application;

import aisafe.aircrafts.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateAircraftStatusUseCaseTest {

    @Mock
    private AircraftRepository repository;

    @InjectMocks
    private UpdateAircraftStatusUseCase updateAircraftStatus;

    private Aircraft buildAircraft() {
        AircraftModel model = new AircraftModel("A320", Manufacturer.AIRBUS, 26730.0, 6150.0, 833.0, "a320.jpg", 180);
        return new Aircraft(AircraftStatus.AVAILABLE, LocalDate.of(2020, 1, 1), model,
                new RegistrationNumber("CS-TPA"), 150, 5000.0, List.of());
    }

    @Test
    void ensureStatusIsUpdatedSuccessfully() {
        when(repository.findByRegistrationNumber(any())).thenReturn(Optional.of(buildAircraft()));
        when(repository.findVersionFor(any())).thenReturn(0L);

        assertDoesNotThrow(() -> updateAircraftStatus.execute(new RegistrationNumber("CS-TPA"), "INACTIVE", 0L));
        verify(repository).save(any(Aircraft.class));
    }

    @Test
    void ensureExceptionWhenAircraftNotFound() {
        when(repository.findByRegistrationNumber(any())).thenReturn(Optional.empty());

        assertThrows(AircraftNotFoundException.class, () ->
                updateAircraftStatus.execute(new RegistrationNumber("CS-XXX"), "INACTIVE", 0L));
        verify(repository, never()).save(any(Aircraft.class));
    }

    @Test
    void ensureExceptionWhenVersionMismatch() {
        when(repository.findByRegistrationNumber(any())).thenReturn(Optional.of(buildAircraft()));
        when(repository.findVersionFor(any())).thenReturn(1L);

        assertThrows(ObjectOptimisticLockingFailureException.class, () ->
                updateAircraftStatus.execute(new RegistrationNumber("CS-TPA"), "INACTIVE", 0L));
        verify(repository, never()).save(any(Aircraft.class));
    }

    @Test
    void ensureExceptionWhenStatusIsInvalid() {
        when(repository.findByRegistrationNumber(any())).thenReturn(Optional.of(buildAircraft()));
        when(repository.findVersionFor(any())).thenReturn(0L);

        assertThrows(AircraftInvalidFieldException.class, () ->
                updateAircraftStatus.execute(new RegistrationNumber("CS-TPA"), "FLYING", 0L));
        verify(repository, never()).save(any(Aircraft.class));
    }
}
