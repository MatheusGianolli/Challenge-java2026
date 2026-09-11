package br.com.challenge.repositories;

import br.com.challenge.models.Veterinario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VeterinarioRepository extends JpaRepository<Veterinario, Long> {

    Page<Veterinario> findByStatusAndEspecialidadeContainingIgnoreCase(
            String status,
            String especialidade,
            Pageable pageable
    );

    Page<Veterinario> findByStatus(
            String status,
            Pageable pageable
    );
}