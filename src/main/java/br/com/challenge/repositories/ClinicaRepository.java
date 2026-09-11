package br.com.challenge.repositories;

import br.com.challenge.models.Clinica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClinicaRepository extends JpaRepository<Clinica, Long> {

    Page<Clinica> findByStatus(
            String status,
            Pageable pageable
    );

    Page<Clinica> findByStatusAndNomeContainingIgnoreCase(
            String status,
            String nome,
            Pageable pageable
    );

    Page<Clinica> findByStatusAndCidadeContainingIgnoreCase(
            String status,
            String cidade,
            Pageable pageable
    );
}