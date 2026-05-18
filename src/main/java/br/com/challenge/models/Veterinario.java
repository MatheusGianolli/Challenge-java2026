package br.com.challenge.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_veterinarios")
@Getter
@Setter
public class Veterinario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String crmv;
    private String especialidade;
    private String tipoAtuacao; // AUTÔNOMO, CLÍNICA ou HÍBRIDO
    private String email;
    private String telefone;
    private String status; // ATIVO ou INATIVO

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dataCadastro;

    private LocalDateTime dataUltimoAcesso;

    public Veterinario() {}
}
