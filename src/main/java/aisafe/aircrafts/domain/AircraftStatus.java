package aisafe.aircrafts.domain;
/**
 * Defines the lifecycle states an aircraft can have in the system.
 */

public enum AircraftStatus {
    AVAILABLE,
    INACTIVE,
    UNDER_MAINTENANCE,
    IN_FLIGHT;

    /**
     * Valida se uma dada String corresponde a um estado válido.
     */
    public static boolean isValid(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        for (AircraftStatus validStatus : AircraftStatus.values()) {
            if (validStatus.name().equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }

}
