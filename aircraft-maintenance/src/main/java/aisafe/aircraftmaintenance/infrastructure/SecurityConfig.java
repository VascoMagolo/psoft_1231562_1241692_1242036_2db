package aisafe.aircraftmaintenance.infrastructure;

import aisafe.shared.infrastructure.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Access rules of the Aircraft &amp; Maintenance service: the aircraft, aircraft model and
 * maintenance rules of the monolith, plus the internal routes used by other services and peers.
 * Tokens are issued by the login service and validated here without calling it.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("""
                ROLE_ADMIN > ROLE_BACKOFFICE_OPERATOR
                ROLE_ADMIN > ROLE_ATCC
                ROLE_ADMIN > ROLE_MAINTENANCE_TECHNICIAN
                ROLE_ADMIN > ROLE_MAINTENANCE_SUPERVISOR
                """);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Replication between replicas of this service: only the replicas themselves (role SERVICE).
                        .requestMatchers("/internal/replicas/**").hasRole("SERVICE")
                        // Internal routes (copies of flights sent by flight-ops, queries from other services).
                        // They carry the user's token; checking the calling service's TLS identity comes with mutual TLS.
                        .requestMatchers("/internal/**").authenticated()
                        // WP #1A - Aircraft
                        .requestMatchers(HttpMethod.POST, "/api/aircrafts").hasRole("ATCC") // US102
                        .requestMatchers(HttpMethod.GET, "/api/aircrafts/search").hasRole("ATCC") // US104 (includes US224)
                        .requestMatchers(HttpMethod.PATCH, "/api/aircrafts/*").hasRole("BACKOFFICE_OPERATOR") // EXTRAS (Update Aircraft)
                        .requestMatchers(HttpMethod.DELETE, "/api/aircrafts/*").hasAnyRole("ATCC", "BACKOFFICE_OPERATOR") // EXTRAS (Delete Aircraft)
                        .requestMatchers(HttpMethod.GET, "/api/aircrafts/*/operational-hours").hasRole("ATCC") // US206
                        .requestMatchers(HttpMethod.GET, "/api/aircrafts/*/utilization").hasRole("ATCC") // US204 (supporting)
                        .requestMatchers(HttpMethod.GET, "/api/aircrafts/*").hasAnyRole("BACKOFFICE_OPERATOR", "ATCC") // US103, US205
                        .requestMatchers(HttpMethod.POST, "/api/aircrafts/import").hasRole("BACKOFFICE_OPERATOR") // US225 (BONUS)
                        .requestMatchers(HttpMethod.GET, "/api/aircrafts").hasAnyRole("ATCC", "BACKOFFICE_OPERATOR") // US103
                        // WP #1A/1B - Aircraft Models
                        .requestMatchers(HttpMethod.POST, "/api/aircraftModels").hasRole("BACKOFFICE_OPERATOR") // US101, US202
                        .requestMatchers(HttpMethod.PATCH, "/api/aircraftModels/*").hasRole("BACKOFFICE_OPERATOR") // US201
                        .requestMatchers(HttpMethod.DELETE, "/api/aircraftModels/*").hasRole("BACKOFFICE_OPERATOR") // EXTRAS (Delete Model)
                        .requestMatchers(HttpMethod.GET, "/api/aircraftModels/top-utilized").hasRole("BACKOFFICE_OPERATOR") // US204
                        .requestMatchers(HttpMethod.POST, "/api/aircraftModels/import").hasRole("BACKOFFICE_OPERATOR") // US225 (BONUS)
                        .requestMatchers(HttpMethod.GET, "/api/aircraftModels/*/image").hasAnyRole("BACKOFFICE_OPERATOR", "ATCC") // US202
                        .requestMatchers(HttpMethod.PATCH, "/api/aircraftModels/*/image").hasRole("BACKOFFICE_OPERATOR") // US202
                        .requestMatchers(HttpMethod.GET, "/api/aircraftModels/*").hasAnyRole("BACKOFFICE_OPERATOR", "ATCC") // US103
                        .requestMatchers(HttpMethod.GET, "/api/aircraftModels").hasAnyRole("BACKOFFICE_OPERATOR", "ATCC") // US103
                        // WP #4A/4B - Maintenance
                        .requestMatchers(HttpMethod.POST, "/api/maintenance/templates/import").hasRole("MAINTENANCE_TECHNICIAN") // US225 (BONUS)
                        .requestMatchers(HttpMethod.POST, "/api/maintenance/records/import").hasRole("MAINTENANCE_TECHNICIAN") // US225 (BONUS)
                        .requestMatchers(HttpMethod.POST, "/api/maintenance/parts/import").hasAnyRole("MAINTENANCE_TECHNICIAN", "MAINTENANCE_SUPERVISOR") // US225 (BONUS), as US226
                        .requestMatchers(HttpMethod.POST, "/api/maintenance/templates").hasRole("MAINTENANCE_TECHNICIAN") // US115b
                        .requestMatchers(HttpMethod.POST, "/api/maintenance/records").hasRole("MAINTENANCE_TECHNICIAN") // US115a
                        .requestMatchers(HttpMethod.POST, "/api/maintenance/parts").hasAnyRole("MAINTENANCE_TECHNICIAN", "MAINTENANCE_SUPERVISOR") // US226 (BONUS)
                        .requestMatchers(HttpMethod.PATCH, "/api/maintenance/records/*").hasRole("MAINTENANCE_TECHNICIAN") // US119
                        .requestMatchers(HttpMethod.PATCH, "/api/maintenance/templates/*").hasRole("MAINTENANCE_TECHNICIAN") // EXTRAS (Update Template)
                        .requestMatchers(HttpMethod.PATCH, "/api/maintenance/parts/*").hasAnyRole("MAINTENANCE_TECHNICIAN", "MAINTENANCE_SUPERVISOR") // US226 (BONUS)
                        .requestMatchers(HttpMethod.DELETE, "/api/maintenance/records/*").hasRole("MAINTENANCE_TECHNICIAN") // EXTRAS (Delete Record)
                        .requestMatchers(HttpMethod.DELETE, "/api/maintenance/templates/*").hasRole("MAINTENANCE_TECHNICIAN") // EXTRAS (Delete Template)
                        .requestMatchers(HttpMethod.DELETE, "/api/maintenance/parts/*").hasAnyRole("MAINTENANCE_TECHNICIAN", "MAINTENANCE_SUPERVISOR") // US226 (BONUS)
                        .requestMatchers(HttpMethod.GET, "/api/maintenance/parts/search").hasAnyRole("MAINTENANCE_TECHNICIAN", "MAINTENANCE_SUPERVISOR", "ATCC") // US226 (BONUS)
                        .requestMatchers(HttpMethod.GET, "/api/maintenance/records/hours").hasRole("ATCC") // US117
                        .requestMatchers(HttpMethod.GET, "/api/maintenance/records/due").hasAnyRole("MAINTENANCE_SUPERVISOR", "MAINTENANCE_TECHNICIAN", "ATCC") // US222
                        .requestMatchers(HttpMethod.GET, "/api/maintenance/records/search").hasAnyRole("MAINTENANCE_TECHNICIAN", "MAINTENANCE_SUPERVISOR", "ATCC") // US218, US217
                        .requestMatchers(HttpMethod.GET, "/api/maintenance/records/ongoing").hasAnyRole("MAINTENANCE_SUPERVISOR", "MAINTENANCE_TECHNICIAN", "ATCC") // US219
                        .requestMatchers(HttpMethod.GET, "/api/maintenance/records/aircraft/*").hasAnyRole("MAINTENANCE_TECHNICIAN", "ATCC") // US116
                        .requestMatchers(HttpMethod.GET, "/api/maintenance/records/cost/aircraft/*").hasAnyRole("ATCC", "BACKOFFICE_OPERATOR", "MAINTENANCE_SUPERVISOR") // US220
                        .requestMatchers(HttpMethod.GET, "/api/maintenance/records/cost/model/*").hasAnyRole("ATCC", "BACKOFFICE_OPERATOR", "MAINTENANCE_SUPERVISOR") // US220
                        .requestMatchers(HttpMethod.GET, "/api/maintenance/records/turnaround/model/*").hasAnyRole("MAINTENANCE_SUPERVISOR", "MAINTENANCE_TECHNICIAN", "ATCC") // US221
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        return http.build();
    }
}
