package com.demande.dmstage.controllers;

import com.demande.dmstage.entities.DemandeStage;
import com.demande.dmstage.services.DemandeStageService;
import com.demande.dmstage.services.ExcelExportService;

import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final DemandeStageService demandeStageService;
    private final ExcelExportService excelExportService;

    public AdminController(DemandeStageService demandeStageService, ExcelExportService excelExportService) {
        this.demandeStageService = demandeStageService;
        this.excelExportService = excelExportService;
    }

    // Dashboard simple
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> dashboardAdmin() {
        Map<String, String> response = Map.of("message", "Bienvenue sur le dashboard de l'admin");
        return ResponseEntity.ok(response);
    }

    // Test simple
    @GetMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("OK ADMIN");
    }

    // Récupérer toutes les demandes (sans filtre pour l’instant)
   @GetMapping("/demandes")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> getAllDemandes(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    try {
        System.out.println("🔐 ADMIN demande la liste des demandes, page=" + page + ", size=" + size);
        Pageable pageable = PageRequest.of(page, size);
        Page<DemandeStage> demandesPage = demandeStageService.getAllDemandes(pageable);
        System.out.println("✅ Nombre de demandes renvoyées dans la page : " + demandesPage.getNumberOfElements());
        return ResponseEntity.ok(demandesPage);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body(Map.of("error", "Erreur interne serveur : " + e.getMessage()));
    }
}


    // Changer le statut d’une demande
   @PutMapping("/demandes/{id}/statut")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> changerStatut(@PathVariable Long id, @RequestBody Map<String, String> body) {
    System.out.println("Appel changerStatut, id=" + id);
    try {
        String statut = body.get("statut");
        System.out.println("Statut reçu: " + statut);

        if (statut == null || statut.isBlank()) {
            System.out.println("Erreur : statut obligatoire");
            return ResponseEntity.badRequest().body(Map.of("error", "Le statut est obligatoire"));
        }

        DemandeStage demande = demandeStageService.changerStatut(id, statut);

        if (demande == null) {
            System.out.println("Demande introuvable avec id=" + id);
            return ResponseEntity.status(404).body(Map.of("error", "Demande non trouvée avec l'id: " + id));
        }

        System.out.println("Statut changé avec succès pour demande id=" + id);
        return ResponseEntity.ok(demande);

    } catch (Exception e) {
        System.out.println("Exception levée dans changerStatut : " + e.getMessage());
        e.printStackTrace();
        // Ne pas renvoyer "Authentication failed" ici, renvoyer une erreur interne serveur
        return ResponseEntity.status(500).body(Map.of("error", "Erreur interne serveur : " + e.getMessage()));
    }
}


    // Recherche avancée multi-critères
    @GetMapping("/demandes/recherche")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DemandeStage>> rechercheDemandes(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String prenom,
            @RequestParam(required = false) String sexe,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String telephone,
            @RequestParam(required = false) String cin,
            @RequestParam(required = false) String adresseDomicile,
            @RequestParam(required = false) String typeStage,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) String duree
    ) {
        List<DemandeStage> resultats = demandeStageService.chercherDemandesAvecCriteres(
                nom, prenom, sexe, email, telephone, cin, adresseDomicile, typeStage, dateDebut, duree);
        return ResponseEntity.ok(resultats);
    }

    // Export des demandes en Excel (filtre dateDemande entre dateDebut et dateFin)
    @GetMapping("/demandes/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exporterDemandesExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin
    ) {
        try {
            List<DemandeStage> demandes;

            if (dateDebut != null && dateFin != null) {
                demandes = demandeStageService.getDemandesEntreDates(dateDebut, dateFin);
            } else {
                demandes = demandeStageService.getAllDemandes(Pageable.unpaged()).getContent();
            }

            byte[] excelContent = excelExportService.exportDemandesToExcel(demandes);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=demandes_stage.xlsx")
                    .contentType(org.springframework.http.MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelContent);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
