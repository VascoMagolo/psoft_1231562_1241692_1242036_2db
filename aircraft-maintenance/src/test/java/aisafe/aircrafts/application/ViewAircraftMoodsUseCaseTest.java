package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.AircraftMoodResponse;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.aircrafts.domain.Manufacturer;
import aisafe.aircrafts.domain.RegistrationNumber;
import aisafe.maintenance.application.MaintenanceRecordsAcrossReplicas;
import aisafe.maintenance.domain.MaintenanceRecord;
import aisafe.maintenance.domain.MaintenanceStatus;
import aisafe.shared.application.Versioned;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ViewAircraftMoodsUseCaseTest {

    private final AircraftAcrossReplicas aircraft = mock(AircraftAcrossReplicas.class);
    private final MaintenanceRecordsAcrossReplicas records = mock(MaintenanceRecordsAcrossReplicas.class);
    private final AircraftModel model = new AircraftModel("Airbus A320neo", Manufacturer.AIRBUS, 26000.0, 6300.0, 830.0, null, 194);

    private Aircraft aircraft(String registration, AircraftStatus status) {
        return new Aircraft(status, LocalDate.of(2020, 1, 1), model, new RegistrationNumber(registration), 180, 6000.0, List.of());
    }

    private Versioned<MaintenanceRecord> record(String registration, MaintenanceStatus status) {
        MaintenanceRecord record = mock(MaintenanceRecord.class);
        when(record.getAircraftRegistration()).thenReturn(new RegistrationNumber(registration));
        when(record.getStatus()).thenReturn(status);
        return new Versioned<>(record, 0L);
    }

    @Test
    void ensureEachAircraftGetsTheMoodOfItsStatusAndMaintenance() {
        when(aircraft.findAll()).thenReturn(List.of(
                aircraft("CS-AAA", AircraftStatus.AVAILABLE),
                aircraft("CS-BBB", AircraftStatus.INACTIVE),
                aircraft("CS-CCC", AircraftStatus.UNDER_MAINTENANCE),
                aircraft("CS-DDD", AircraftStatus.IN_FLIGHT),
                aircraft("CS-EEE", AircraftStatus.AVAILABLE),
                aircraft("CS-FFF", AircraftStatus.AVAILABLE)));
        List<Versioned<MaintenanceRecord>> maintenance = List.of(
                record("CS-EEE", MaintenanceStatus.PLANNED),
                record("CS-FFF", MaintenanceStatus.IN_PROGRESS),
                record("CS-AAA", MaintenanceStatus.COMPLETED));
        when(records.findAll()).thenReturn(maintenance);

        Map<String, String> moods = new ViewAircraftMoodsUseCase(aircraft, records).execute().stream()
                .collect(Collectors.toMap(AircraftMoodResponse::registrationNumber, AircraftMoodResponse::mood));

        assertEquals(Map.of("CS-AAA", "HAPPY", "CS-BBB", "SLEEPY", "CS-CCC", "GRUMPY", "CS-DDD", "EXCITED",
                "CS-EEE", "ANXIOUS", "CS-FFF", "GRUMPY"), moods);
    }

    @Test
    void ensureResponseNamesTheAircraftItsModelAndStatus() {
        when(aircraft.findAll()).thenReturn(List.of(aircraft("CS-AAA", AircraftStatus.AVAILABLE)));
        when(records.findAll()).thenReturn(List.of());

        AircraftMoodResponse mood = new ViewAircraftMoodsUseCase(aircraft, records).execute().getFirst();

        assertEquals(new AircraftMoodResponse("CS-AAA", "Airbus A320neo", "AVAILABLE", "HAPPY", "Ready to fly"), mood);
    }
}
