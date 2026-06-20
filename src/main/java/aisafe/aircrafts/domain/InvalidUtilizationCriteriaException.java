package aisafe.aircrafts.domain;

import aisafe.shared.domain.DomainException;

public class InvalidUtilizationCriteriaException extends DomainException {
    public InvalidUtilizationCriteriaException(String message) {
        super(message);
    }
}
