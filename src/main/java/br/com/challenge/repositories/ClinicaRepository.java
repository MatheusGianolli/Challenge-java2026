package br.com.challenge.repositories;

import br.com.challenge.models.Clinica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClinicaRepository extends JpaRepository<Clinica, Long> {

    // REQUISITO DA SPRINT: Busca com parâmetros
    Page<Clinica> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
