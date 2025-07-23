package com.demande.dmstage.controllers;

import com.demande.dmstage.entities.DemandeStage;
import com.demande.dmstage.services.DemandeStageService;
import com.demande.dmstage.services.ExcelExportService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final DemandeStageService demandeStageService;
    private final ExcelExportService excelExportService;


    // Injection via constructeur
    public AdminController(DemandeStageService demandeStageService, ExcelExportService excelExportService) {
        this.demandeStageService = demandeStageService;
        this.excelExportService = excelExportService;
    }

    // Accueil / Dashboard Admin simple
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> dashboardAdmin() {
        Map<String, String> response = Map.of("message", "Bienvenue sur le dashboard de l'admin");
        return ResponseEntity.ok(response);
    }

    // Lister toutes les demandes - accessible uniquement par admin
    @GetMapping("/demandes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DemandeStage>> getAllDemandes() {
        List<DemandeStage> demandes = demandeStageService.getAllDemandes();
        return ResponseEntity.ok(demandes);
    }

    @GetMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("OK ADMIN");
    }

    // Modifier le statut d'une demande (ex: EN_ATTENTE, ACCEPTE, REFUSE)
    @PutMapping("/demandes/{id}/statut")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changerStatut(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String statut = body.get("statut");
            if (statut == null || statut.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le statut est obligatoire"));
            }
            DemandeStage demande = demandeStageService.changerStatut(id, statut);
            return ResponseEntity.ok(demande);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // Recherche avancée avec plusieurs critères
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

    // Exporter les demandes en Excel avec filtre dateDemande entre dateDebut et dateFin
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
                demandes = demandeStageService.getAllDemandes();
            }

            byte[] excelContent = excelExportService.exportDemandesToExcel(demandes);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=demandes_stage.xlsx")
                    .contentType(org.springframework.http.MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelContent);

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

}
