package br.com.challenge.dtos;

import br.com.challenge.models.Pet;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PetDTO {

    public record Request(
            @NotNull(message = "ID do responsável é obrigatório") Long responsavelId,
            @NotBlank(message = "Nome é obrigatório") String nome,
            @NotBlank(message = "Espécie é obrigatória") String especie,
            String raca,
            LocalDate dataNascimento,
            Double peso,
            Double altura,
            @NotBlank(message = "Sexo é obrigatório") String sexo,
            String porte,
            String statusSaude,
            String alergias,
            String castrado,
            String corPelagem,
            String microchip
    ) {}

    public record Response(
            Long id, String nomeResponsavel, String nome, String especie, String raca,
            LocalDate dataNascimento, Double peso, Double altura, String sexo,
            String porte, String statusSaude, String alergias, String castrado,
            String corPelagem, String microchip, LocalDateTime dataCadastro,
            Long responsavelId
    ) {
        public Response(Pet p) {
            this(p.getId(), p.getResponsavel() != null ? p.getResponsavel().getNome() : null,
                    p.getNome(), p.getEspecie(), p.getRaca(), p.getDataNascimento(),
                    p.getPeso(), p.getAltura(), p.getSexo(), p.getPorte(), p.getStatusSaude(),
                    p.getAlergias(), p.getCastrado(), p.getCorPelagem(), p.getMicrochip(), p.getDataCadastro(),
                    p.getResponsavel() != null ? p.getResponsavel().getId() : null);
        }
    }

    /**
     * Prontuário do pet: dados de saúde, contato do responsável e
     * histórico completo de atendimentos (cuidado contínuo).
     */
    public record Historico(
            Response pet,
            String telefoneResponsavel,
            String emailResponsavel,
            long totalConsultas,
            long consultasRealizadas,
            LocalDateTime ultimoAtendimento,
            LocalDateTime proximaConsulta,
            LocalDateTime proximoRetorno,
            List<ConsultaResponseDTO> consultas
    ) {}
}
