package br.com.challenge.dtos;

import br.com.challenge.models.Clinica;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class ClinicaDTO {

    public record Request(
            @NotBlank(message = "Nome da clínica é obrigatório") String nome,
            @NotBlank(message = "CNPJ é obrigatório") String cnpj,
            @NotBlank(message = "E-mail é obrigatório") @Email(message = "E-mail inválido") String email,
            @NotBlank(message = "Telefone é obrigatório") String telefone,
            String logradouro,
            String numeroEndereco,
            String bairro,
            String cidade,
            String estado,
            String cep,
            String tipoEstabelecimento,
            String especialidadePrincipal,
            String atendimento24h,
            String atendeEmergencia
    ) {}

    public record Response(
            Long id, String nome, String cnpj, String email, String telefone,
            String logradouro, String numeroEndereco, String bairro, String cidade,
            String estado, String cep, String status, LocalDateTime dataCadastro,
            String tipoEstabelecimento, String especialidadePrincipal,
            String atendimento24h, String atendeEmergencia
    ) {
        public Response(Clinica c) {
            this(c.getId(), c.getNome(), c.getCnpj(), c.getEmail(), c.getTelefone(),
                    c.getLogradouro(), c.getNumeroEndereco(), c.getBairro(), c.getCidade(),
                    c.getEstado(), c.getCep(), c.getStatus(), c.getDataCadastro(),
                    c.getTipoEstabelecimento(), c.getEspecialidadePrincipal(),
                    c.getAtendimento24h(), c.getAtendeEmergencia());
        }
    }
}