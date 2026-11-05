package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.AircraftMoodResponse;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.maintenance.application.MaintenanceRecordsAcrossReplicas;
import aisafe.maintenance.domain.MaintenanceRecord;
import aisafe.maintenance.domain.MaintenanceStatus;
import aisafe.shared.application.UseCase;
import aisafe.shared.application.Versioned;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The mood of every aircraft ({@code GET /api/v2/unicorns}, the mock endpoint the Assignment 2
 * asks for). A query and nothing else: it reads the query side (the fleet and the maintenance
 * records across replicas, which with events on is this replica's read model) and never the write
 * model, nor changes anything.
 *
 * <ul>
 *   <li>{@code SLEEPY}: inactive;</li>
 *   <li>{@code GRUMPY}: under maintenance, or a maintenance is in progress;</li>
 *   <li>{@code EXCITED}: in flight;</li>
 *   <li>{@code ANXIOUS}: available, with a maintenance planned;</li>
 *   <li>{@code HAPPY}: available, nothing planned.</li>
 * </ul>
 */
@UseCase(readOnly = true)
public class ViewAircraftMoodsUseCase {

    private final AircraftAcrossReplicas aircraft;
    private final MaintenanceRecordsAcrossReplicas records;

    public ViewAircraftMoodsUseCase(AircraftAcrossReplicas aircraft, MaintenanceRecordsAcrossReplicas records) {
        this.aircraft = aircraft;
        this.records = records;
    }

    /** Every aircraft with its mood, ordered by registration. */
    public List<AircraftMoodResponse> execute() {
        Map<MaintenanceStatus, Set<String>> byStatus = records.findAll().stream()
                .map(Versioned::value)
                .collect(Collectors.groupingBy(MaintenanceRecord::getStatus,
                        Collectors.mapping(r -> r.getAircraftRegistration().getNumber(), Collectors.toSet())));
        Set<String> inProgress = byStatus.getOrDefault(MaintenanceStatus.IN_PROGRESS, Set.of());
        Set<String> planned = byStatus.getOrDefault(MaintenanceStatus.PLANNED, Set.of());
        return aircraft.findAll().stream()
                .map(a -> moodOf(a, inProgress, planned))
                .toList();
    }

    private static AircraftMoodResponse moodOf(Aircraft aircraft, Set<String> inProgress, Set<String> planned) {
        String registration = aircraft.getRegistrationNumber().getNumber();
        AircraftStatus status = aircraft.getStatus();
        String mood;
        String reason;
        if (status == AircraftStatus.INACTIVE) {
            mood = "SLEEPY";
            reason = "Not in service";
        } else if (status == AircraftStatus.UNDER_MAINTENANCE || inProgress.contains(registration)) {
            mood = "GRUMPY";
            reason = "Stuck in the hangar";
        } else if (status == AircraftStatus.IN_FLIGHT) {
            mood = "EXCITED";
            reason = "Flying";
        } else if (planned.contains(registration)) {
            mood = "ANXIOUS";
            reason = "Maintenance coming up";
        } else {
            mood = "HAPPY";
            reason = "Ready to fly";
        }
        return new AircraftMoodResponse(registration, aircraft.getModel().getModelName(), status.name(), mood, reason);
    }
}
