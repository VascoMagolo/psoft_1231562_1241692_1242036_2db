package aisafe.aircrafts.application;

import aisafe.aircrafts.application.dtos.FleetStatusAircraftResponse;
import aisafe.aircrafts.application.dtos.FleetStatusGroupResponse;
import aisafe.aircrafts.application.dtos.FleetStatusResponse;
import aisafe.aircrafts.domain.Aircraft;
import aisafe.aircrafts.domain.AircraftRepository;
import aisafe.aircrafts.domain.AircraftStatus;
import aisafe.shared.application.UseCase;
import aisafe.shared.domain.PaginatedResult;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Returns a fleet-wide status overview, grouping all aircraft by their operational status.
 * Every status is always present in the result, even when no aircraft currently holds it.
 */
@UseCase(readOnly = true)
public class ViewFleetStatusUseCase {

    private final AircraftRepository repository;

    public ViewFleetStatusUseCase(AircraftRepository repository) {
        this.repository = repository;
    }

    /**
     * Builds the fleet status overview.
     *
     * @return a response containing all aircraft grouped by their current operational status
     */
    public FleetStatusResponse execute() {
        List<Aircraft> allAircraft = repository.findAll();

        Map<AircraftStatus, List<Aircraft>> grouped = allAircraft.stream()
                .collect(Collectors.groupingBy(Aircraft::getStatus));

        List<FleetStatusGroupResponse> groups = Arrays.stream(AircraftStatus.values())
                .map(status -> {
                    List<FleetStatusAircraftResponse> aircraftDtos = grouped
                            .getOrDefault(status, List.of())
                            .stream()
                            .map(a -> new FleetStatusAircraftResponse(
                                    a.getRegistrationNumber().getNumber(),
                                    a.getModel().getModelName(),
                                    a.getModel().getManufacturer()))
                            .toList();
                    return new FleetStatusGroupResponse(
                            status,
                            new PaginatedResult<>(aircraftDtos, aircraftDtos.size()));
                })
                .toList();

        return new FleetStatusResponse(
                allAircraft.size(),
                new PaginatedResult<>(groups, groups.size()));
    }
}
