package br.com.challenge.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_clinicas")
@Getter
@Setter
public class Clinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String cnpj;

    private String email;

    private String telefone;

    // Endereço
    private String logradouro;

    @Column(name = "numero_endereco")
    private String numeroEndereco;

    private String bairro;

    private String cidade;

    private String estado;

    private String cep;

    private String status; // ATIVO ou INATIVO

    @CreationTimestamp
    @Column(name = "data_cadastro", updatable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "tipo_estabelecimento")
    private String tipoEstabelecimento; // Clínica veterinária, hospital etc.

    @Column(name = "especialidade_principal")
    private String especialidadePrincipal;

    @Column(name = "atendimento_24h")
    private String atendimento24h; // S ou N

    @Column(name = "atende_emergencia")
    private String atendeEmergencia; // S ou N

    public Clinica() {
    }
}