package com.demande.dmstage.services;

import com.demande.dmstage.entities.Utilisateur;
import com.demande.dmstage.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Optional;

@Service
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean verifierMotDePasse(String email, String motDePasseSaisi) {
        return utilisateurRepository.findByEmail(email)
                .map(utilisateur -> passwordEncoder.matches(motDePasseSaisi, utilisateur.getMotDePasse()))
                .orElse(false);
    }

    public Optional<Utilisateur> authentifier(String email, String motDePasse) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmail(email);

        if (utilisateurOpt.isPresent()) {
            Utilisateur utilisateur = utilisateurOpt.get();
            if (passwordEncoder.matches(motDePasse, utilisateur.getMotDePasse())) {
                return Optional.of(utilisateur);
            }
        }
        return Optional.empty();
    }

    public Utilisateur inscrire(Utilisateur utilisateur) {
        if ("ADMIN".equalsIgnoreCase(utilisateur.getRole())) {
            throw new RuntimeException("Inscription refusée pour le rôle ADMIN");
        }

        if (utilisateur.getMotDePasse() != null && !utilisateur.getMotDePasse().startsWith("$2a$")) {
            String motDePasseEncode = passwordEncoder.encode(utilisateur.getMotDePasse());
            utilisateur.setMotDePasse(motDePasseEncode);
        }

        return utilisateurRepository.save(utilisateur);
    }

    public Optional<Utilisateur> trouverParEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    public Optional<Utilisateur> trouverParId(Long id) {
        return utilisateurRepository.findById(id);
    }

    public boolean existeParEmail(String email) {
        return utilisateurRepository.findByEmail(email).isPresent();
    }
}

@Component
class PasswordTest {

    private final PasswordEncoder passwordEncoder;

    public PasswordTest(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void generateHash() {
        String motDePasseClair = "admin1234";
        String hash = passwordEncoder.encode(motDePasseClair);
        System.out.println("Hash BCrypt généré : " + hash);
    }
}
