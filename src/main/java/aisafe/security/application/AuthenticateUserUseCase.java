package aisafe.security.application;


import aisafe.UseCase;
import aisafe.SuppressArgLogging;
import aisafe.security.application.dtos.AuthResponse;
import aisafe.security.application.dtos.LoginRequest;
import aisafe.security.domain.InvalidCredentialsException;
import aisafe.security.domain.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application use case that authenticates a patron by username and password and returns a JWT token.
 *
 * <p>Annotated with {@link SuppressArgLogging} so that {@link aisafe.UseCaseLoggingAdvice}
 * logs {@code [SUPPRESSED]} in place of the raw credentials instead of printing them to the
 * log. {@link UseCase} still applies, giving this class consistent transaction management,
 * Bean Validation, and timing logs.</p>
 *
 * <p>The message on failure is intentionally generic — do not distinguish between "username
 * not found" and "wrong password" to prevent username enumeration attacks.</p>
 */
@UseCase
@SuppressArgLogging
@Transactional(readOnly = true)
public class AuthenticateUserUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticateUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    /**
     * Verifies the credentials and returns a signed JWT token on success.
     *
     * @param request the login request containing username and password
     * @return a {@link AuthResponse} containing the signed JWT
     * @throws InvalidCredentialsException if the username is not found or the password does not match
     */
    public AuthResponse execute(LoginRequest request) {
        var patron = userRepository.findByUsername(request.username())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), patron.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return new AuthResponse(jwtService.generateToken(patron));
    }


}
