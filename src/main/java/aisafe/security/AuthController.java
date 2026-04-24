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

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticateUserUseCase AuthenticateUser;
    private final RegisterUserUseCase registerUser;
    public AuthController(AuthenticateUserUseCase authenticateUser, RegisterUserUseCase registerUser) {
        this.AuthenticateUser = authenticateUser;
        this.registerUser = registerUser;
    }
    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return registerUser.execute(request);
    }
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return AuthenticateUser.execute(request);
    }
}
