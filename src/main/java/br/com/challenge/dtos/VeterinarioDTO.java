package br.com.challenge.dtos;

import br.com.challenge.models.Veterinario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class VeterinarioDTO {

    public record Request(
            @NotBlank(message = "Nome é obrigatório") String nome,
            @NotBlank(message = "CRMV é obrigatório") String crmv,
            String especialidade,
            String tipoAtuacao,
            @NotBlank(message = "E-mail é obrigatório") @Email(message = "E-mail inválido") String email,
            @NotBlank(message = "Telefone é obrigatório") String telefone
    ) {}

    public record Response(
            Long id, String nome, String crmv, String especialidade,
            String tipoAtuacao, String email, String telefone,
            String status, LocalDateTime dataCadastro, LocalDateTime dataUltimoAcesso
    ) {
        public Response(Veterinario v) {
            this(v.getId(), v.getNome(), v.getCrmv(), v.getEspecialidade(),
                    v.getTipoAtuacao(), v.getEmail(), v.getTelefone(),
                    v.getStatus(), v.getDataCadastro(), v.getDataUltimoAcesso());
        }
    }
}
