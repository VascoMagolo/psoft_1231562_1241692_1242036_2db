package aisafe.security.infrastructure;

import aisafe.security.domain.User;
import aisafe.security.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationConfigTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ApplicationConfig applicationConfig;

    @Test
    void ensureUserDetailsServiceLoadsUserWhenExists() {
        User mockUser = mock(User.class);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        UserDetailsService service = applicationConfig.userDetailsService();
        assertNotNull(service);
        assertEquals(mockUser, service.loadUserByUsername("testuser"));
    }

    @Test
    void ensureUserDetailsServiceThrowsUsernameNotFoundExceptionWhenUserDoesNotExist() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        UserDetailsService service = applicationConfig.userDetailsService();
        assertNotNull(service);
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("nonexistent"));
    }

    @Test
    void ensureAuthenticationProviderIsConfigured() {
        AuthenticationProvider provider = applicationConfig.authenticationProvider();
        assertNotNull(provider);
    }

    @Test
    void ensurePasswordEncoderIsBCrypt() {
        PasswordEncoder encoder = applicationConfig.passwordEncoder();
        assertNotNull(encoder);
        String rawPassword = "password123";
        String encoded = encoder.encode(rawPassword);
        assertTrue(encoder.matches(rawPassword, encoded));
    }

    @Test
    void ensureAuthenticationManagerIsRetrieved() throws Exception {
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager expectedManager = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(expectedManager);

        AuthenticationManager actualManager = applicationConfig.authenticationManager(authConfig);
        assertEquals(expectedManager, actualManager);
    }
}
