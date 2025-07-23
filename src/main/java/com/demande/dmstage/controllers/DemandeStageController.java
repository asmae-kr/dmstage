package com.demande.dmstage.controllers;

import com.demande.dmstage.entities.DemandeStage;
import com.demande.dmstage.entities.DemandeStage.TypeStage;
import com.demande.dmstage.services.DemandeStageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/demandes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DemandeStageController {

    private final DemandeStageService demandeStageService;

    // ✅ Méthode pour convertir String en enum TypeStage
    private TypeStage convertirStringEnTypeStage(String typeStageStr) {
        if (typeStageStr == null) {
            return TypeStage.NORMAL; // valeur par défaut
        }
        try {
            return TypeStage.valueOf(typeStageStr.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return TypeStage.NORMAL; // valeur par défaut si invalide
        }
    }

    // ✅ POST : Ajouter une demande avec upload de fichiers
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<DemandeStage> ajouterDemande(
            @RequestParam("nom") String nom,
            @RequestParam("prenom") String prenom,
            @RequestParam("sexe") String sexe,
            @RequestParam("email") String email,
            @RequestParam("telephone") String telephone,
            @RequestParam("cin") String cin,
            @RequestParam("adresseDomicile") String adresseDomicile,
            @RequestParam("typeStage") String typeStageStr,
            @RequestParam("dateDebut") String dateDebut,
            @RequestParam("duree") String duree,
            @RequestParam("conventionStage") MultipartFile conventionStage,
            @RequestParam("demandeStage") MultipartFile demandeStage,
            @RequestParam("cv") MultipartFile cv,
            @RequestParam("lettreMotivation") MultipartFile lettreMotivation,
            @RequestParam("cinRecto") MultipartFile cinRecto,
            @RequestParam("cinVerso") MultipartFile cinVerso,
            @RequestParam("photo") MultipartFile photo
    ) throws IOException {

        DemandeStage demande = new DemandeStage();
        demande.setNom(nom);
        demande.setPrenom(prenom);
        demande.setSexe(sexe);
        demande.setEmail(email);
        demande.setTelephone(telephone);
        demande.setCin(cin);
        demande.setAdresseDomicile(adresseDomicile);
        demande.setTypeStage(convertirStringEnTypeStage(typeStageStr));
        demande.setDateDebut(LocalDate.parse(dateDebut));
        demande.setDuree(duree);

        String uploadDir = "uploads/";
        Files.createDirectories(Paths.get(uploadDir)); // Crée le dossier si nécessaire

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
        return ResponseEntity.ok(saved);
    }

    // ✅ GET : Toutes les demandes
    @GetMapping
    public ResponseEntity<List<DemandeStage>> getAllDemandes() {
        return ResponseEntity.ok(demandeStageService.getAllDemandes());
    }

    // ✅ GET : Demandes par email
    @GetMapping("/email/{email}")
    public ResponseEntity<List<DemandeStage>> getDemandesParEmail(@PathVariable String email) {
        return ResponseEntity.ok(demandeStageService.getDemandesParEmail(email));
    }

    // ✅ PUT : Mise à jour du statut
    @PutMapping("/{id}/statut")
    public ResponseEntity<String> changerStatut(
            @PathVariable Long id,
            @RequestParam String nouveauStatut) {
        demandeStageService.changerStatut(id, nouveauStatut);
        return ResponseEntity.ok("Statut mis à jour avec succès");
    }
}
