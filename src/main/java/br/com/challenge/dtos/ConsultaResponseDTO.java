package br.com.challenge.dtos;

import br.com.challenge.models.Consulta;
import java.time.LocalDateTime;

public record ConsultaResponseDTO(
        Long id,
        String nomeResponsavel,
        String nomePet,
        String nomeClinica,
        String nomeVeterinario,
        LocalDateTime dataCriacao,
        LocalDateTime dataPrevista,
        LocalDateTime retornoPrevisto,
        String tipoConsulta,
        String descricaoSintomas,
        String diagnostico,
        String status
) {
    // Construtor que transforma a Entidade no DTO automaticamente
    public ConsultaResponseDTO(Consulta c) {
        this(
                c.getId(),
                c.getResponsavel() != null ? c.getResponsavel().getNome() : null,
                c.getPet() != null ? c.getPet().getNome() : null,
                c.getClinica() != null ? c.getClinica().getNome() : null,
                c.getVeterinario() != null ? c.getVeterinario().getNome() : null,
                c.getDataCriacao(),
                c.getDataPrevista(),
                c.getRetornoPrevisto(),
                c.getTipoConsulta(),
                c.getDescricaoSintomas(),
                c.getDiagnostico(),
                c.getStatus()
        );
    }
}
