package com.demande.dmstage.services;

import com.demande.dmstage.entities.DemandeStage;
import com.demande.dmstage.entities.DemandeStage.Statut; // importe ton enum
import com.demande.dmstage.repositories.DemandeStageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DemandeStageService {

   /*  private final EmailService emailService;*/
    private final DemandeStageRepository demandeStageRepository;

    // Convertit une String en enum Statut, ou renvoie EN_ATTENTE par défaut
    private Statut convertirStringEnStatut(String statutStr) {
        if (statutStr == null) {
            return Statut.EN_ATTENTE;
        }
        try {
            return Statut.valueOf(statutStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            // si valeur inconnue, retourner EN_ATTENTE par défaut ou lever une exception selon besoin
            return Statut.EN_ATTENTE;
        }
    }

    // Ajouter une nouvelle demande - par défaut statut "EN_ATTENTE"
    public DemandeStage ajouterDemande(DemandeStage demande) {
        if (demande.getStatut() == null) {
            demande.setStatut(Statut.EN_ATTENTE);
        }
        return demandeStageRepository.save(demande);
    }

    // Récupérer toutes les demandes (pour l'admin)
    public Page<DemandeStage> getAllDemandes(Pageable pageable) {
    Page<DemandeStage> demandesPage = demandeStageRepository.findAll(pageable);
    System.out.println("🔍 Nombre total de demandes dans la page : " + demandesPage.getNumberOfElements());
    return demandesPage;
}

    // Récupérer les demandes d’un utilisateur par son email
    public List<DemandeStage> getDemandesParEmail(String email) {
        List<DemandeStage> demandes = demandeStageRepository.findByEmail(email);
        System.out.println("📩 Demandes récupérées pour l'email " + email + " : " + demandes.size());
        return demandes;
    }

    // Changer le statut d'une demande et notifier par email
public DemandeStage changerStatut(Long id, String statutStr) {
    Optional<DemandeStage> optional = demandeStageRepository.findById(id);

    if (optional.isPresent()) {
        DemandeStage demande = optional.get();
        // Utiliser la méthode pour convertir la String en Statut
        Statut statutEnum = convertirStringEnStatut(statutStr);
        demande.setStatut(statutEnum);
        DemandeStage saved = demandeStageRepository.save(demande);

        // Envoi d'email de notification
       /*  String contenu = "Bonjour " + demande.getNom() + ",\n\n"
                + "Votre demande de stage a été mise à jour.\n"
                + "👉 Nouveau statut : " + statutEnum.name() + "\n\n"
                + "Merci de votre confiance.";
        emailService.envoyerEmail(demande.getEmail(), "Mise à jour de votre demande de stage", contenu);
*/
        return saved;
    } else {
        throw new RuntimeException("Demande non trouvée avec l'id: " + id);
    }
}

    public List<DemandeStage> chercherDemandesAvecCriteres(
        String nom,
        String prenom,
        String sexe,
        String email,
        String telephone,
        String cin,
        String adresseDomicile,
        String typeStage,
        LocalDate dateDebut,
        String duree) {
        return demandeStageRepository.chercherAvecCriteres(
            nom, prenom, sexe, email, telephone, cin, adresseDomicile, typeStage, dateDebut, duree);
    }
    public void sauvegarderDemande(DemandeStage demande) {
    demandeStageRepository.save(demande);
}
public DemandeStage getDemandeParId(Long id) {
    return demandeStageRepository.findById(id).orElse(null);
}



    public List<DemandeStage> getDemandesEntreDates(LocalDate debut, LocalDate fin) {
        return demandeStageRepository.findByDateDemandeBetween(debut, fin);
    }
}
