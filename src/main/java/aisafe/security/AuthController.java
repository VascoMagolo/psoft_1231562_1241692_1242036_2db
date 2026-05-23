package aisafe.security;

import aisafe.security.application.AuthenticateUserUseCase;
import aisafe.security.application.RegisterUserUseCase;
import aisafe.security.application.dtos.AuthResponse;
import aisafe.security.application.dtos.LoginRequest;
import aisafe.security.application.dtos.RegisterRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsible for handling authentication-related HTTP requests
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticateUserUseCase AuthenticateUser;
    private final RegisterUserUseCase registerUser;
    public AuthController(AuthenticateUserUseCase authenticateUser, RegisterUserUseCase registerUser) {
        this.AuthenticateUser = authenticateUser;
        this.registerUser = registerUser;
    }

    /**
     * Endpoint for user registration.
     * @param request the registration request containing the user information
     * @return an AuthResponse containing the authentication token for the newly registered user
     */
    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return registerUser.execute(request);
    }

    /**
     * Endpoint for user login.
     * @param request the login request containing the username and password
     * @return an AuthResponse containing the authentication token for the authenticated user
     */
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return AuthenticateUser.execute(request);
    }
}
