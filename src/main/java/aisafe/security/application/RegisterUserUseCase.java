package aisafe.security.application;

import aisafe.UseCase;
import aisafe.security.application.dtos.AuthResponse;
import aisafe.security.application.dtos.RegisterRequest;
import aisafe.security.domain.User;
import aisafe.security.domain.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for registering a new user in the system.
 */
@UseCase
@Transactional
public class RegisterUserUseCase {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public RegisterUserUseCase(UserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new user with the provided details
     * @param request the registration request containing the information of the new user
     * @return an authentication response containing a JWT token for the newly registered user
     */
    public AuthResponse execute(RegisterRequest request) {
        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.role()
        );
        repository.save(user);

        return new AuthResponse(jwtService.generateToken(user));
    }
}