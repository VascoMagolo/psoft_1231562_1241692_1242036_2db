package aisafe;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as an application use case.
 *
 * <p>This is a composed annotation that combines:</p>
 * <ul>
 *   <li>{@link Service} — registers the class as a Spring-managed bean</li>
 *   <li>{@link Validated} — enables Bean Validation on method parameters</li>
 *   <li>{@link Transactional} — wraps every use case method in a single database transaction,
 *       so that domain event listeners registered with
 *       {@code @TransactionalEventListener(phase = BEFORE_COMMIT)} run inside that same
 *       transaction and any failure rolls back the entire operation atomically</li>
 * </ul>
 *
 * <p>Any class annotated with {@code @UseCase} is also intercepted by
 * {@link UseCaseLoggingAdvice}, which logs the method name, parameters, and execution time
 * for every public method invocation — without any log statements in the use case code itself.</p>
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
@Validated
@Transactional
public @interface UseCase {
}
