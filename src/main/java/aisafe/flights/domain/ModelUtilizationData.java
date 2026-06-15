package aisafe.flights.domain;

/**
 * Domain read model for aircraft model utilization data.
 */
public record ModelUtilizationData(String modelName, Long utilizationValue) {
}
