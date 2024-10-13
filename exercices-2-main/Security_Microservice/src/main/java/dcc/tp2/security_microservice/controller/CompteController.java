package dcc.tp2.security_microservice.controller;

import dcc.tp2.security_microservice.Entity.Compte;
import dcc.tp2.security_microservice.Service.CompteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/comptes")
public class CompteController {

    @Autowired
    private CompteService compteService;

    // Accessible uniquement pour les ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Compte> creerCompte(@RequestBody Compte compte) {
        Compte nouveauCompte = compteService.creerCompte(compte);
        return ResponseEntity.ok(nouveauCompte);
    }

    // Accessible pour tous les utilisateurs authentifiés
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/solde")
    public ResponseEntity<Double> consulterSolde(@PathVariable Long id) {
        Optional<Compte> compte = compteService.consulterSolde(id);
        return compte.map(value -> ResponseEntity.ok(value.getSolde()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Accessible pour tous les utilisateurs authentifiés
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/crediter/{montant}")
    public ResponseEntity<Compte> crediter(@PathVariable Long id, @PathVariable double montant) {
        Compte compte = compteService.crediter(id, montant);
        return ResponseEntity.ok(compte);
    }

    // Accessible pour tous les utilisateurs authentifiés
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/debiter/{montant}")
    public ResponseEntity<Compte> debiter(@PathVariable Long id, @PathVariable double montant) {
        Compte compte = compteService.debiter(id, montant);
        return ResponseEntity.ok(compte);
    }

    // Accessible uniquement pour les ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Compte>> recupererTousLesComptes() {
        List<Compte> comptes = compteService.recupererTousLesComptes();
        return ResponseEntity.ok(comptes);
    }
}
