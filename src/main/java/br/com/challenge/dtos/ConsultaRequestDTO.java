package br.com.challenge.dtos;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ConsultaRequestDTO(
        @NotNull(message = "ID do Responsável é obrigatório")
        Long responsavelId,

        @NotNull(message = "ID do Pet é obrigatório")
        Long petId,

        @NotNull(message = "ID da Clínica é obrigatório")
        Long clinicaId,

        @NotNull(message = "ID do Veterinário é obrigatório")
        Long veterinarioId,

        @NotNull(message = "A data prevista é obrigatória")
        @Future(message = "A data prevista deve ser no futuro")
        LocalDateTime dataPrevista,

        @NotBlank(message = "O tipo de consulta é obrigatório")
        String tipoConsulta,

        String descricaoSintomas
) {}
