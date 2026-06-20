package aisafe.security.application;

import aisafe.security.application.dtos.AuthResponse;
import aisafe.security.application.dtos.RegisterRequest;
import aisafe.security.domain.Role;
import aisafe.security.domain.User;
import aisafe.security.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private RegisterUserUseCase useCase;

    @Test
    void ensureUserIsRegisteredSuccessfully() {
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = useCase.execute(new RegisterRequest("newuser", "pass", Role.ATCC));

        assertNotNull(response);
        assertEquals("jwt-token", response.token());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(captor.capture());
        assertEquals("newuser", captor.getValue().getUsername());
        assertEquals(Role.ATCC, captor.getValue().getRole());
    }
}
