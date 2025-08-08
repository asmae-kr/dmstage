package com.demande.dmstage.controllers;

import com.demande.dmstage.entities.Utilisateur;
import com.demande.dmstage.services.CustomUserDetailsService;
import com.demande.dmstage.security.JwtUtil;
import com.demande.dmstage.services.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UtilisateurService utilisateurService;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthController(AuthenticationManager authenticationManager,
                          UtilisateurService utilisateurService,
                          JwtUtil jwtUtil,
                          CustomUserDetailsService customUserDetailsService) {
        this.authenticationManager = authenticationManager;
        this.utilisateurService = utilisateurService;
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Utilisateur utilisateur) {
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
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String motDePasse = credentials.get("motDePasse");

            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, motDePasse)
            );

            Utilisateur utilisateur = utilisateurService.trouverParEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

            String token = jwtUtil.generateToken(userDetails);

            return ResponseEntity.ok(Map.of(
                "token", token,
                "id", utilisateur.getId(),
                "email", utilisateur.getEmail(),
                "role", utilisateur.getRole()
            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("message", "Email ou mot de passe invalide"));
        }
    }

    @GetMapping("/user/profile")
    public ResponseEntity<Utilisateur> getMonProfil(Authentication authentication) {
        return utilisateurService.trouverParEmail(authentication.getName())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
