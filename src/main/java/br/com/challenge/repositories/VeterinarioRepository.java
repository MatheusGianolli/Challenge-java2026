package br.com.challenge.repositories;

import br.com.challenge.models.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface VeterinarioRepository extends JpaRepository<Veterinario, Long> {


        Page<Veterinario> findByEspecialidadeContainingIgnoreCase(String especialidade, Pageable pageable);
    }


