package com.demande.dmstage.repositories;

import com.demande.dmstage.entities.DemandeStage;
import com.demande.dmstage.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DemandeStageRepository extends JpaRepository<DemandeStage, Long> {
  
    List<DemandeStage> findByUtilisateur(Utilisateur utilisateur);  // OK

    List<DemandeStage> findByEmail(String email); // optionnel pour suivi par email
    List<DemandeStage> findByDateDemandeBetween(LocalDate dateDebut, LocalDate dateFin);


    @Query("""
    SELECT d FROM DemandeStage d
    WHERE (:nom IS NULL OR LOWER(d.nom) LIKE LOWER(CONCAT('%', :nom, '%')))
    AND (:prenom IS NULL OR LOWER(d.prenom) LIKE LOWER(CONCAT('%', :prenom, '%')))
    AND (:sexe IS NULL OR d.sexe = :sexe)
    AND (:email IS NULL OR LOWER(d.email) LIKE LOWER(CONCAT('%', :email, '%')))
    AND (:telephone IS NULL OR LOWER(d.telephone) LIKE LOWER(CONCAT('%', :telephone, '%')))
    AND (:cin IS NULL OR LOWER(d.cin) LIKE LOWER(CONCAT('%', :cin, '%')))
    AND (:adresseDomicile IS NULL OR LOWER(d.adresseDomicile) LIKE LOWER(CONCAT('%', :adresseDomicile, '%')))
    AND (:typeStage IS NULL OR LOWER(d.typeStage) LIKE LOWER(CONCAT('%', :typeStage, '%')))
    AND ((:dateDebut IS NULL OR :dateFin IS NULL) OR d.dateDebut BETWEEN :dateDebut AND :dateFin)
    AND (:duree IS NULL OR LOWER(d.duree) LIKE LOWER(CONCAT('%', :duree, '%')))
""")

    List<DemandeStage> chercherAvecCriteres(
            @Param("nom") String nom,
            @Param("prenom") String prenom,
            @Param("sexe") String sexe,
            @Param("email") String email,
            @Param("telephone") String telephone,
            @Param("cin") String cin,
            @Param("adresseDomicile") String adresseDomicile,
            @Param("typeStage") String typeStage,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("duree") String duree
    );
}
