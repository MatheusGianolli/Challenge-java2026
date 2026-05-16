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

    // Busca consultas de uma clínica específica com paginação
    Page<Consulta> findByClinicaId(Long clinicaId, Pageable pageable);

    // Validação de negócio (JPQL): Verifica se o Veterinário já tem consulta naquele horário
    @Query("SELECT c FROM Consulta c WHERE c.veterinario.id = :vetId AND c.dataPrevista = :dataPrevista AND c.status != 'CANCELADA'")
    List<Consulta> buscarConflitoDeHorario(@Param("vetId") Long vetId, @Param("dataPrevista") LocalDateTime dataPrevista);
}
