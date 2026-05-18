package br.com.challenge.dtos;

import br.com.challenge.models.Tutor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TutorDTO {

    public record Request(
            @NotBlank(message = "Nome é obrigatório") String nome,
            String tipoDocumento,
            @NotBlank(message = "Documento é obrigatório") String numeroDocumento,
            LocalDate dataNascimento,
            @NotBlank(message = "E-mail é obrigatório") @Email(message = "E-mail inválido") String email,
            @NotBlank(message = "Telefone é obrigatório") String telefone,
            String tipoResponsavel,
            String preferenciaContato,
            String recebeNotificacao,
            String autorizaMonitoramento,
            String logradouro,
            String numeroEndereco,
            String bairro,
            String cidade,
            String estado,
            String cep
    ) {}

    public record Response(
            Long id, String nome, String tipoDocumento, String numeroDocumento,
            LocalDate dataNascimento, String email, String telefone, String tipoResponsavel,
            String preferenciaContato, String recebeNotificacao, String autorizaMonitoramento,
            String logradouro, String numeroEndereco, String bairro, String cidade,
            String estado, String cep, String status, LocalDateTime dataCadastro, LocalDateTime dataUltimoAcesso
    ) {
        public Response(Tutor t) {
            this(t.getId(), t.getNome(), t.getTipoDocumento(), t.getNumeroDocumento(),
                    t.getDataNascimento(), t.getEmail(), t.getTelefone(), t.getTipoResponsavel(),
                    t.getPreferenciaContato(), t.getRecebeNotificacao(), t.getAutorizaMonitoramento(),
                    t.getLogradouro(), t.getNumeroEndereco(), t.getBairro(), t.getCidade(),
                    t.getEstado(), t.getCep(), t.getStatus(), t.getDataCadastro(), t.getDataUltimoAcesso());
        }
    }
}
