package aisafe.security.application;

import aisafe.security.application.dtos.AuthResponse;
import aisafe.security.application.dtos.LoginRequest;
import aisafe.security.domain.InvalidCredentialsException;
import aisafe.security.domain.Role;
import aisafe.security.domain.User;
import aisafe.security.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordVerifier passwordVerifier;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticateUserUseCase useCase;

    @Test
    void ensureValidCredentialsReturnToken() {
        User user = new User("admin", "encoded", Role.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordVerifier.matches("pass", "encoded")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = useCase.execute(new LoginRequest("admin", "pass"));

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
    }

    @Test
    void ensureUnknownUsernameThrowsInvalidCredentials() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> useCase.execute(new LoginRequest("unknown", "pass")));
    }

    @Test
    void ensureWrongPasswordThrowsInvalidCredentials() {
        User user = new User("admin", "encoded", Role.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordVerifier.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> useCase.execute(new LoginRequest("admin", "wrong")));
    }
}
