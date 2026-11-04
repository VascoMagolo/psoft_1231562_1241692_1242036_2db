package aisafe.fleet.application;

import aisafe.fleet.domain.AircraftInfo;

import java.util.Optional;

/**
 * What this service needs to know about the fleet, which belongs to aircraft-maintenance.
 *
 * <p>Without events (Assignment 1) it is asked to aircraft-maintenance on every call
 * ({@code HttpFleet}, C1 and C3), and when aircraft-maintenance cannot be reached both methods throw
 * {@link aisafe.fleet.domain.FleetUnavailableException}: an unknown answer is never taken as
 * "does not exist". With events (Assignment 2) it is read from this replica's copy, kept by the
 * events of aircraft-maintenance ({@code ReadModelFleet}): always answered, maybe a few moments
 * behind.</p>
 */
public interface Fleet {

    /** The aircraft with this registration (contract C1), or empty if it does not exist. */
    Optional<AircraftInfo> findAircraft(String registration);

    /** Whether an aircraft model with this name exists (contract C3). */
    boolean aircraftModelExists(String modelName);
}
