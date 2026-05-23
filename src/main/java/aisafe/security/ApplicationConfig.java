package aisafe.security;

import aisafe.security.domain.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration class for setting up authentication and user details services in the application.
 */
@Configuration
public class ApplicationConfig {

    private final UserRepository userRepository;

    public ApplicationConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Defines a UserDetailsService bean that retrieves user details from the UserRepository based on the provided username.
     * @return a UserDetailsService implementation that fetches user details from the database
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Defines an AuthenticationProvider bean that uses a DaoAuthenticationProvider to authenticate users based on the UserDetailsService and PasswordEncoder.
     * @return an AuthenticationProvider that authenticates users using the configured UserDetailsService and PasswordEncoder
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Defines an AuthenticationManager bean that retrieves the authentication manager from the provided AuthenticationConfiguration.
     * @param config the AuthenticationConfiguration from which to retrieve the AuthenticationManager
     * @return the AuthenticationManager configured for the application
     * @throws Exception if there is an error retrieving the AuthenticationManager from the configuration
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Defines a PasswordEncoder bean that uses BCryptPasswordEncoder to encode passwords securely.
     * @return a PasswordEncoder that uses BCrypt for password hashing
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}