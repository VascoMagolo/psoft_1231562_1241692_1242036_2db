package aisafe;

import aisafe.security.application.AuthenticateUserUseCase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * Marks a {@link UseCase}-annotated class whose method arguments must not be logged.
 *
 * <p>Apply this annotation to any use case that handles sensitive input (e.g. credentials)
 * to prevent {@link UseCaseLoggingAdvice} from logging raw argument values. The advice will
 * log {@code [SUPPRESSED]} in place of the actual arguments while still recording the method
 * name and execution time.</p>
 *
 * <p>Example: {@link AuthenticateUserUseCase} carries both
 * {@code @UseCase} and {@code @SuppressArgLogging} so that login credentials are never
 * written to the log.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SuppressArgLogging {
}
