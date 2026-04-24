package aisafe;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Arrays;

/**
 * AOP aspect that logs the execution of every public method in every {@link UseCase}-annotated
 * class.
 *
 * <p>For each use-case invocation the advice logs:</p>
 * <ul>
 *   <li>The class and method name together with the arguments, on entry</li>
 *   <li>The class and method name together with the elapsed time (ms), on exit</li>
 * </ul>
 *
 * <p>This is a cross-cutting concern: no use case class needs a single log statement — the
 * logging is woven in at runtime by Spring AOP. {@code @Order(1)} ensures this advice runs
 * before any other advice applied to the same pointcut.</p>
 */
@Slf4j
@Component
@Aspect
@Order(1)
public class UseCaseLoggingAdvice {

    /** Matches any class annotated with {@link UseCase}. */
    @Pointcut("within(@aisafe.UseCase *)")
    public void useCase() {
    }

    /** Matches any public method. */
    @Pointcut("execution(public * *(..))")
    public void publicMethod() {
    }

    /** Matches public methods inside a {@link UseCase}-annotated class. */
    @Pointcut("publicMethod() && useCase()")
    public void publicMethodInsideAUseCase() {
    }

    /**
     * Logs the use-case method name, parameters, and execution duration.
     *
     * @param pjp the join point representing the intercepted method call
     * @return the value returned by the intercepted method
     * @throws Throwable any exception thrown by the intercepted method is re-thrown unchanged
     */
    @Around("publicMethodInsideAUseCase()")
    public Object aroundServiceMethodAdvice(final ProceedingJoinPoint pjp) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        try {
            boolean suppress = pjp.getTarget().getClass().isAnnotationPresent(SuppressArgLogging.class);
            String args = suppress ? "[SUPPRESSED]" : Arrays.toString(pjp.getArgs());
            log.info("Executing use case: {}#{} with parameters: {}", pjp.getTarget().getClass(), pjp.getSignature().getName(), args);
            stopWatch.start();
            return pjp.proceed();
        } finally {
            stopWatch.stop();
            log.info("Finished executing use case {}#{} in {}ms", pjp.getTarget().getClass(), pjp.getSignature().getName(), stopWatch.getTotalTimeMillis());
        }
    }
}