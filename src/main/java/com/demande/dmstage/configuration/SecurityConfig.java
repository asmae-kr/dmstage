package com.demande.dmstage.configuration;

import com.demande.dmstage.services.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Désactive CSRF pour une API REST
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/register", "/api/login").permitAll() // Routes publiques
                .requestMatchers("/api/demandes/**").permitAll()            // ✅ TEMPORAIRE : accès libre pour tester
                .requestMatchers("/api/admin/**").hasRole("ADMIN")          // Routes ADMIN
                .requestMatchers("/api/user/**").hasRole("USER")            // Routes USER
                .anyRequest().authenticated()                               // Le reste est protégé
            )
            .userDetailsService(userDetailsService)                         // Ton UserDetailsService personnalisé
            .formLogin(form -> form.disable())                              // Pas de formulaire par défaut
            .httpBasic(basic -> {});                                        // Basic Auth (ou ton filtre perso)

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
