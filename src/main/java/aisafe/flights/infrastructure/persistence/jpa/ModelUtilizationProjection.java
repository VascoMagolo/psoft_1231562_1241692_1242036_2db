package aisafe.flights.infrastructure.persistence.jpa;

/**
 * JPA Projection for model utilization metrics.
 */
public interface ModelUtilizationProjection {
    String getModelName();
    Long getUtilizationValue();
}
