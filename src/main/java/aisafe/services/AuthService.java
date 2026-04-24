package aisafe.services;

import aisafe.security.application.dtos.AuthResponse;
import aisafe.security.application.dtos.LoginRequest;
import aisafe.security.application.dtos.RegisterRequest;
import aisafe.security.domain.User;
import aisafe.security.domain.UserRepository;
import aisafe.security.application.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository repository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuthenticationManager authenticationManager) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        // creates user with hashed password
        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.role()
        );
        repository.save(user);

        // generates jwt access token
        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }

    public AuthResponse login(LoginRequest request) {
        // spring security verifies auto. if password is correct, if not, stops here
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        // this gets the user from the Database
        User user = repository.findByUsername(request.username())
                .orElseThrow();

        // Generates and gets a token for the user
        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }
}