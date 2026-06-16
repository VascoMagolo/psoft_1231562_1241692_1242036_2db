package aisafe.shared.application;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link UseCase}-annotated class whose method arguments must not be logged.
 *
 * <p>Apply this annotation to any use case that handles sensitive input (e.g. credentials)
 * to prevent logging raw argument values. The logging infrastructure will
 * log {@code [SUPPRESSED]} in place of the actual arguments while still recording the method
 * name and execution time.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SuppressArgLogging {
}
