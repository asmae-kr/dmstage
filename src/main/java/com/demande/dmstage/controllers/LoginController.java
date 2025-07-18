package com.demande.dmstage.controllers;

import com.demande.dmstage.entities.Utilisateur;
import com.demande.dmstage.services.UtilisateurService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class LoginController {

    private final UtilisateurService utilisateurService;

    public LoginController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";  // login.html
    }

    @GetMapping("/inscription")
    public String inscriptionForm(Model model) {
        model.addAttribute("utilisateur", new Utilisateur());
        return "inscription"; // inscription.html
    }

    @PostMapping("/inscription")
    public String inscriptionSubmit(@ModelAttribute Utilisateur utilisateur, Model model) {
        try {
            utilisateurService.creerCompte(utilisateur);
            return "redirect:/login?inscriptionReussie";
        } catch (Exception e) {
            model.addAttribute("error", "Email déjà utilisé");
            return "inscription";
        }
    }
}

