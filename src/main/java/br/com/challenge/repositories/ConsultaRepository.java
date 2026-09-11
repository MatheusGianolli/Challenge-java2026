package br.com.challenge.repositories;

import br.com.challenge.models.Consulta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    // Lista consultas de uma clínica com paginação.
    Page<Consulta> findByClinicaId(
            Long clinicaId,
            Pageable pageable
    );

    // Lista consultas de um veterinário com paginação.
    Page<Consulta> findByVeterinarioId(
            Long veterinarioId,
            Pageable pageable
    );

    // Lista consultas de um pet com paginação.
    Page<Consulta> findByPetId(
            Long petId,
            Pageable pageable
    );

    // Lista consultas por status.
    Page<Consulta> findByStatus(
            String status,
            Pageable pageable
    );

    // Verifica se o veterinário já possui consulta no mesmo horário.
    // Consultas canceladas não bloqueiam o horário.
    @Query("""
            SELECT c
            FROM Consulta c
            WHERE c.veterinario.id = :vetId
              AND c.dataPrevista = :dataPrevista
              AND c.status <> 'CANCELADA'
            """)
    List<Consulta> buscarConflitoDeHorario(
            @Param("vetId") Long vetId,
            @Param("dataPrevista") LocalDateTime dataPrevista
    );
}