package com.demande.dmstage.services;

import com.demande.dmstage.entities.Utilisateur;
import com.demande.dmstage.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + email));

        String role = formatRole(utilisateur.getRole());

        System.out.println("Authentification utilisateur : " + email + ", rôle : " + role);

        GrantedAuthority authority = new SimpleGrantedAuthority(role);

        return User.builder()
                .username(utilisateur.getEmail())
                .password(utilisateur.getMotDePasse())
                .authorities(Collections.singletonList(authority))
                .build();
    }

    /**
     * Ajoute le préfixe "ROLE_" si ce n'est pas déjà présent.
     * Si rôle null ou vide, retourne "ROLE_USER" par défaut.
     */
    private String formatRole(String role) {
        if (role == null || role.isBlank()) {
            return "ROLE_USER";  // Rôle par défaut avec préfixe
        }
        if (!role.startsWith("ROLE_")) {
            return "ROLE_" + role;  // Ajoute préfixe s'il manque
        }
        return role;
    }
}
