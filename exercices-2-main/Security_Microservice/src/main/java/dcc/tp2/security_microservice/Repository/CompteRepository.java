package dcc.tp2.security_microservice.Repository;


import dcc.tp2.security_microservice.Entity.Compte;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompteRepository extends JpaRepository<Compte, Long> {

}