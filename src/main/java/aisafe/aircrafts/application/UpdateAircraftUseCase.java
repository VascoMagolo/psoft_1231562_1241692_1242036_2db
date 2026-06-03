package aisafe.aircrafts.application;


import aisafe.shared.application.UseCase;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case that updates the aircraft information.
 * Can update multiple fields or only one.
 */

@UseCase
@Transactional(readOnly = true)
public class UpdateAircraftUseCase {
}
