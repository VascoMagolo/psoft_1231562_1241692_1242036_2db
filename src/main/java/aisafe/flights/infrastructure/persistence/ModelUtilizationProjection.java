package aisafe.flights.infrastructure.persistence;

/**
 * JPA Projection for model utilization metrics.
 */
public interface ModelUtilizationProjection {
    String getModelName();
    Long getUtilizationValue();
}
