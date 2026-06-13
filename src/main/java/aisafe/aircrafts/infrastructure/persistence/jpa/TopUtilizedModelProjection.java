package aisafe.aircrafts.infrastructure.persistence.jpa;

public interface TopUtilizedModelProjection {
    String getModelName();
    Long getUtilizationValue();
}
