package com.example.Securitate.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req
                        .requestMatchers("/api/v1/auth/**").permitAll()  // Permite accesul la autentificare și înregistrare
                        .requestMatchers(HttpMethod.GET, "/api/v1/car").permitAll()      // Acces liber pentru GET pe /api/v1/car
                        .requestMatchers(HttpMethod.POST, "/api/v1/car").permitAll()   // Permite accesul la POST pentru ADMIN și USER
                        .requestMatchers(HttpMethod.PUT, "/api/v1/car/**").permitAll()   // Permite accesul la PUT pentru ADMIN și USER
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/car/**").permitAll()   // Permite accesul la DELETE pentru ADMIN și USER
                        .requestMatchers(HttpMethod.GET, "/api/v1/car/*/image").permitAll() // Acces liber pentru imagini
                        .requestMatchers(HttpMethod.GET, "/api/v1/car/*").permitAll()
                        .requestMatchers( "/api/v1/rezervari").permitAll() // Acces liber pentru detaliile unei mașini
                        .anyRequest().authenticated()  // Orice altă cerere necesită autentificare
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
