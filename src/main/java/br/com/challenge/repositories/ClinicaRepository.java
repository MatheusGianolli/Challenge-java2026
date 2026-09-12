package br.com.challenge.repositories;

import br.com.challenge.models.Clinica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClinicaRepository extends JpaRepository<Clinica, Long> {

    // Busca clínicas por status.
    Page<Clinica> findByStatus(
            String status,
            Pageable pageable
    );

    // Busca clínicas por status e nome.
    Page<Clinica> findByStatusAndNomeContainingIgnoreCase(
            String status,
            String nome,
            Pageable pageable
    );

    // Busca clínicas por status e cidade.
    Page<Clinica> findByStatusAndCidadeContainingIgnoreCase(
            String status,
            String cidade,
            Pageable pageable
    );

    // Busca clínicas de qualquer status pelo nome.
    Page<Clinica> findByNomeContainingIgnoreCase(
            String nome,
            Pageable pageable
    );

    // Busca clínicas de qualquer status pela cidade.
    Page<Clinica> findByCidadeContainingIgnoreCase(
            String cidade,
            Pageable pageable
    );
}