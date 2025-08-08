package com.demande.dmstage.services;

import com.demande.dmstage.entities.Utilisateur;
import com.demande.dmstage.repositories.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Recherche utilisateur par email (unique)
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + email));

        // Formatte le rôle pour qu'il soit sans le préfixe ROLE_ (pour éviter ROLE_ROLE_)
        String role = formatRole(utilisateur.getRole());

        // Debug : affiche dans la console le rôle chargé
        System.out.println("Authentification utilisateur : " + email + ", rôle : " + role);

        // Retourne UserDetails avec username, mot de passe hashé et rôle
        return User.builder()
                .username(utilisateur.getEmail())
                .password(utilisateur.getMotDePasse())
                .roles(role) // Spring ajoute automatiquement "ROLE_" devant
                .build();
    }

    /**
     * Si le rôle est null, retourne "USER" par défaut.
     * Si le rôle commence par "ROLE_", on le nettoie pour éviter "ROLE_ROLE_"
     */
    private String formatRole(String role) {
        if (role == null || role.isBlank()) {
            return "USER"; // Rôle par défaut
        }
        if (role.startsWith("ROLE_")) {
            return role.substring(5);
        }
        return role;
    }
}
