package com.demande.dmstage.controllers;

import com.demande.dmstage.dto.DemandeStageDTO;
import com.demande.dmstage.entities.DemandeStage;
import com.demande.dmstage.entities.DemandeStage.TypeStage;
import com.demande.dmstage.services.DemandeStageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/demandes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DemandeStageController {

    private final DemandeStageService demandeStageService;

    // mémoire temporaire
    private final ConcurrentHashMap<String, DemandeStage> demandeTempMap = new ConcurrentHashMap<>();

    private TypeStage convertirStringEnTypeStage(String typeStageStr) {
        if (typeStageStr == null) return TypeStage.NORMAL;
        try {
            return TypeStage.valueOf(typeStageStr.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return TypeStage.NORMAL;
        }
    }

    @PostMapping("/info")
    public ResponseEntity<String> recevoirInfosDemande(@RequestBody DemandeStageDTO dto) {
        DemandeStage demande = new DemandeStage();
        demande.setNom(dto.getNom());
        demande.setPrenom(dto.getPrenom());
        demande.setSexe(dto.getSexe());
        demande.setEmail(dto.getEmail());
        demande.setTelephone(dto.getTelephone());
        demande.setCin(dto.getCin());
        demande.setAdresseDomicile(dto.getAdresseDomicile());
        demande.setTypeStage(convertirStringEnTypeStage(dto.getTypeStage()));
        demande.setDateDebut(LocalDate.parse(dto.getDateDebut()));
        demande.setDuree(dto.getDuree());
        demandeStageService.sauvegarderDemande(demande);

        demandeTempMap.put(dto.getEmail(), demande);
        return ResponseEntity.ok("Données personnelles reçues avec succès.");
    }

    @PostMapping("/upload")
    public ResponseEntity<?> recevoirFichiers(
            @RequestParam("email") String email,
            @RequestParam("conventionStage") MultipartFile conventionStage,
            @RequestParam("demandeStage") MultipartFile demandeStage,
            @RequestParam("cv") MultipartFile cv,
            @RequestParam("lettreMotivation") MultipartFile lettreMotivation,
            @RequestParam("cinRecto") MultipartFile cinRecto,
            @RequestParam("cinVerso") MultipartFile cinVerso,
            @RequestParam("photo") MultipartFile photo
    ) throws IOException {

        if (!demandeTempMap.containsKey(email)) {
            return ResponseEntity.badRequest().body("Aucune demande temporaire trouvée pour cet email.");
        }

        DemandeStage demande = demandeTempMap.get(email);

        String uploadDir = "uploads/";
        Files.createDirectories(Paths.get(uploadDir));

        Files.copy(conventionStage.getInputStream(), Paths.get(uploadDir, conventionStage.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
        demande.setConventionStage(conventionStage.getOriginalFilename());

        Files.copy(demandeStage.getInputStream(), Paths.get(uploadDir, demandeStage.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
        demande.setDemandeStage(demandeStage.getOriginalFilename());

        Files.copy(cv.getInputStream(), Paths.get(uploadDir, cv.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
        demande.setCv(cv.getOriginalFilename());

        Files.copy(lettreMotivation.getInputStream(), Paths.get(uploadDir, lettreMotivation.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
        demande.setLettreMotivation(lettreMotivation.getOriginalFilename());

        Files.copy(cinRecto.getInputStream(), Paths.get(uploadDir, cinRecto.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
        demande.setCinRecto(cinRecto.getOriginalFilename());

        Files.copy(cinVerso.getInputStream(), Paths.get(uploadDir, cinVerso.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
        demande.setCinVerso(cinVerso.getOriginalFilename());

        Files.copy(photo.getInputStream(), Paths.get(uploadDir, photo.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
        demande.setPhoto(photo.getOriginalFilename());

        DemandeStage saved = demandeStageService.ajouterDemande(demande);
        demandeTempMap.remove(email);

        return ResponseEntity.ok(saved);
    }

   @GetMapping
public ResponseEntity<List<DemandeStage>> getAllDemandes() {
    Pageable pageable = PageRequest.of(0, 10); // page 0, taille 10
    Page<DemandeStage> page = demandeStageService.getAllDemandes(pageable);
    return ResponseEntity.ok(page.getContent()); // .getContent() pour retourner une liste
}


    @GetMapping("/email/{email}")
    public ResponseEntity<List<DemandeStage>> getDemandesParEmail(@PathVariable String email) {
        return ResponseEntity.ok(demandeStageService.getDemandesParEmail(email));
    }

   @GetMapping("/demandes/{id}/statut")
@PreAuthorize("hasRole('USER')")
public ResponseEntity<?> consulterStatut(@PathVariable Long id, Authentication authentication) {
    String emailUtilisateur = authentication.getName(); // Email de l'utilisateur connecté

    DemandeStage demande = demandeStageService.getDemandeParId(id);

    if (demande == null) {
        return ResponseEntity.status(404).body(Map.of("error", "Demande non trouvée"));
    }

    if (!demande.getEmail().equals(emailUtilisateur)) {
        return ResponseEntity.status(403).body(Map.of("error", "Accès interdit à cette demande"));
    }

    return ResponseEntity.ok(Map.of(
        "id", demande.getId(),
        "nom", demande.getNom(),
        "prenom", demande.getPrenom(),
        "statut", demande.getStatut().name()
    ));
}

}
