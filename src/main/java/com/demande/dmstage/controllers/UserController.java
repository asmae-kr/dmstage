package com.demande.dmstage.controllers;

import com.demande.dmstage.entities.DemandeStage;
import com.demande.dmstage.services.DemandeStageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {

    private final DemandeStageService demandeStageService;

    // ✅ Récupère les demandes du user connecté
    @GetMapping("/mes-demandes")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<DemandeStage>> getMesDemandes(Authentication authentication) {
        String email = authentication.getName(); // récupère l'email sécurisé de l'utilisateur connecté
        List<DemandeStage> demandes = demandeStageService.getDemandesParEmail(email);
        return ResponseEntity.ok(demandes);
    }

    // ✅ Ajoute une demande en forçant l'email de l'utilisateur connecté
    @PostMapping("/demande")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<DemandeStage> ajouterDemande(@RequestBody DemandeStage demande, Authentication authentication) {
        String emailConnecte = authentication.getName(); // email authentifié
        demande.setEmail(emailConnecte); // ⚠️ On ignore l'email envoyé depuis le front
        DemandeStage saved = demandeStageService.ajouterDemande(demande);
        return ResponseEntity.ok(saved);
    }
}
