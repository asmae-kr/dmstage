package com.demande.dmstage.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.demande.dmstage.services.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
                                    throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }

        // Vérifie si l'utilisateur n'est pas encore authentifié dans le contexte
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Option 1 : Charger UserDetails depuis la base (option existante)
            // UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            // if (jwtUtil.validateToken(jwt, userDetails)) {
            //     UsernamePasswordAuthenticationToken authToken =
            //             new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            //     authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            //     SecurityContextHolder.getContext().setAuthentication(authToken);
            // }

            // Option 2 (recommandée ici) : Construire un UserDetails simple à partir des infos du token, sans accès base

            String role = jwtUtil.extractRole(jwt); // ex: ROLE_ADMIN ou ROLE_USER
            if (role == null || role.isBlank()) {
                role = "ROLE_USER"; // fallback
            }

            // Crée une authority avec le rôle extrait
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

            // Crée un UserDetails "léger" avec username et authorities
            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(username)
                    .password("") // Pas nécessaire ici, on valide via token
                    .authorities(Collections.singletonList(authority))
                    .build();

            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
