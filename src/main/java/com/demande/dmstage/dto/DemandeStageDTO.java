package com.demande.dmstage.dto;

import lombok.Data;

@Data
public class DemandeStageDTO {
    private String nom;
    private String prenom;
    private String sexe;
    private String email;
    private String telephone;
    private String cin;
    private String adresseDomicile;
    private String typeStage;
    private String dateDebut;
    private String duree;
}
