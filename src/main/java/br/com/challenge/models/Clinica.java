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
    private String numeroEndereco;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;

    private String status; // ATIVO ou INATIVO

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dataCadastro;

    private String tipoEstabelecimento; // clínica veterinária, hospital, etc.
    private String especialidadePrincipal;
    private String atendimento24h; // S ou N
    private String atendeEmergencia; // S ou N

    public Clinica() {}
}
