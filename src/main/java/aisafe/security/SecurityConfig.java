package aisafe.security;

import aisafe.security.infrastructure.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, AuthenticationProvider authenticationProvider) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // can be added or changed requests based on USs just make sure it has proper roles
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // WP #1A – Aircraft Management
                        .requestMatchers(HttpMethod.POST, "/api/aircrafts/register").hasAnyRole("ATCC", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/aircrafts/search").hasAnyRole("ATCC", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/aircrafts/*/status").hasAnyRole("ATCC", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/aircrafts/*").hasAnyRole("BACKOFFICE_OPERATOR", "ATCC", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/aircrafts").hasAnyRole("ATCC", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/aircraftModels").hasAnyRole("BACKOFFICE_OPERATOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/aircraftModels").hasAnyRole("BACKOFFICE_OPERATOR", "ATCC", "ADMIN")
                        // WP #2 – Airports
                        .requestMatchers(HttpMethod.POST, "/api/airports").hasAnyRole("BACKOFFICE_OPERATOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/airports/*/certifications").hasAnyRole("BACKOFFICE_OPERATOR", "ATCC", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/airports/search").hasAnyRole("ATCC", "BACKOFFICE_OPERATOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/airports/statistics/**").hasAnyRole("BACKOFFICE_OPERATOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/airports/grouped").hasAnyRole("ATCC", "BACKOFFICE_OPERATOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/airports/*/routes").hasAnyRole("ATCC", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/airports/*").hasAnyRole("BACKOFFICE_OPERATOR", "ATCC", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/airports/*/status").hasAnyRole("BACKOFFICE_OPERATOR", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/airports/*/details").hasAnyRole("BACKOFFICE_OPERATOR", "ADMIN")
                        // Maintenance
                        .requestMatchers("/api/maintenance/**").hasAnyRole("MAINTENANCE_TECHNICIAN", "MAINTENANCE_SUPERVISOR")
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        return http.build();
    }
}