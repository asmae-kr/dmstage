package com.demande.dmstage.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "demande_stage")
@Data
@NoArgsConstructor
public class DemandeStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;
    private String sexe;
    private String email;
    private String telephone;
    private String cin;
    private String adresseDomicile;

    @Enumerated(EnumType.STRING)
    private TypeStage typeStage;

    private LocalDate dateDebut;
    private String duree;

    private String conventionStage;    // nom/fichier de la convention
    private String demandeStage;       // nom/fichier de la demande
    private String cv;                 // nom/fichier du CV
    private String lettreMotivation;   // nom/fichier lettre de motivation
    private String cinRecto;           // nom/fichier recto CIN
    private String cinVerso;           // nom/fichier verso CIN
    private String photo;              // nom/fichier photo

    @Enumerated(EnumType.STRING)
    private Statut statut;

    private LocalDate dateDemande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    @JsonIgnore
    private Utilisateur utilisateur;

    @PrePersist
    public void prePersist() {
        if (this.dateDemande == null) {
            this.dateDemande = LocalDate.now();
        }
        if (this.statut == null) {
            this.statut = Statut.EN_ATTENTE;
        }
    }

    // Enum pour type de stage
    public enum TypeStage {
        NORMAL,
        OBSERVATION,
        INITIATION,
        FIN_ETUDE,
        PFE
    }

    // Enum pour statut
    public enum Statut {
        EN_ATTENTE,
        ACCEPTE,
        REFUSE
    }
}
