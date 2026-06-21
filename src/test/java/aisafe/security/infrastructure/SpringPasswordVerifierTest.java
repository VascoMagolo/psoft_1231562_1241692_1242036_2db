package aisafe.security.infrastructure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpringPasswordVerifierTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SpringPasswordVerifier springPasswordVerifier;

    @Test
    void ensureMatchesCallsPasswordEncoder() {
        when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);
        assertTrue(springPasswordVerifier.matches("raw", "encoded"));
        verify(passwordEncoder).matches("raw", "encoded");

        when(passwordEncoder.matches("raw", "wrong")).thenReturn(false);
        assertFalse(springPasswordVerifier.matches("raw", "wrong"));
    }
}
