package com.demande.dmstage.controllers;

import com.demande.dmstage.entities.Utilisateur;
import com.demande.dmstage.services.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UtilisateurService utilisateurService;

    public AuthController(AuthenticationManager authenticationManager, UtilisateurService utilisateurService) {
        this.authenticationManager = authenticationManager;
        this.utilisateurService = utilisateurService;
    }

    // Endpoint pour inscription d’un nouvel utilisateur
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Utilisateur utilisateur) {
        try {
            if (utilisateurService.existeParEmail(utilisateur.getEmail())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Cet email est déjà utilisé."));
            }

            Utilisateur inscrit = utilisateurService.inscrire(utilisateur);
            return ResponseEntity.ok(Map.of(
                "message", "Inscription réussie",
                "id", inscrit.getId(),
                "email", inscrit.getEmail(),
                "role", inscrit.getRole()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Erreur serveur : " + e.getMessage()));
        }
    }

    // Endpoint pour authentification / login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String motDePasse = credentials.get("motDePasse");

            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, motDePasse)
            );

            Utilisateur utilisateur = utilisateurService.trouverParEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable après authentification"));

            return ResponseEntity.ok(Map.of(
                "message", "Authentification réussie",
                "id", utilisateur.getId(),
                "email", utilisateur.getEmail(),
                "role", utilisateur.getRole()
            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("message", "Email ou mot de passe invalide"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Erreur serveur : " + e.getMessage()));
        }
    }

    // Endpoint pour récupérer le profil de l’utilisateur connecté
    @GetMapping("/user/profile")
    public ResponseEntity<Utilisateur> getMonProfil(Authentication authentication) {
        return utilisateurService.trouverParEmail(authentication.getName())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
