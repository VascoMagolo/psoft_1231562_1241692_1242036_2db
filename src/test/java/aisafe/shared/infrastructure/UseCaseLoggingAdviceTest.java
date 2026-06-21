package aisafe.shared.infrastructure;

import aisafe.shared.application.SuppressArgLogging;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UseCaseLoggingAdviceTest {

    private final UseCaseLoggingAdvice advice = new UseCaseLoggingAdvice();

    @SuppressArgLogging
    static class SuppressedTarget {}

    static class NormalTarget {}

    @Test
    void ensureAroundLogsNormalTarget() throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(pjp.getTarget()).thenReturn(new NormalTarget());
        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("execute");
        when(pjp.getArgs()).thenReturn(new Object[]{"arg1"});
        when(pjp.proceed()).thenReturn("result");

        Object result = advice.aroundServiceMethodAdvice(pjp);

        assertEquals("result", result);
        verify(pjp).proceed();
        
        advice.useCase();
        advice.publicMethod();
        advice.publicMethodInsideAUseCase();
    }

    @Test
    void ensureAroundLogsSuppressedTarget() throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(pjp.getTarget()).thenReturn(new SuppressedTarget());
        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("execute");
        when(pjp.proceed()).thenReturn("result");

        Object result = advice.aroundServiceMethodAdvice(pjp);

        assertEquals("result", result);
        verify(pjp).proceed();
    }
}
