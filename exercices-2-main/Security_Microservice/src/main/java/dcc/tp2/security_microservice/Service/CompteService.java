package dcc.tp2.security_microservice.Service;

import dcc.tp2.security_microservice.Entity.Compte;

import dcc.tp2.security_microservice.Repository.CompteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CompteService {

    @Autowired
    private CompteRepository compteRepository;

    public Compte creerCompte(Compte compte) {
        return compteRepository.save(compte);
    }

    public Optional<Compte> consulterSolde(Long id) {
        return compteRepository.findById(id);
    }

    public Compte crediter(Long id, double montant) {
        Compte compte = compteRepository.findById(id).orElseThrow(() -> new RuntimeException("Compte non trouvé"));
        compte.setSolde(compte.getSolde() + montant);
        return compteRepository.save(compte);
    }

    public Compte debiter(Long id, double montant) {
        Compte compte = compteRepository.findById(id).orElseThrow(() -> new RuntimeException("Compte non trouvé"));
        if (compte.getSolde() < montant) {
            throw new RuntimeException("Fonds insuffisants");
        }
        compte.setSolde(compte.getSolde() - montant);
        return compteRepository.save(compte);
    }
}
