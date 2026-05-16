package br.com.challenge.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_consultas")
@Getter
@Setter// novamente usando o lombok para deixar o codigo mais limpo e menos volumoso onde ele é respomsavel pelos getters and setters
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Chaves Estrangeiras (FK)
    @ManyToOne
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Tutor responsavel;

    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne
    @JoinColumn(name = "clinica_id", nullable = false)
    private Clinica clinica;

    @ManyToOne
    @JoinColumn(name = "veterinario_id", nullable = false)
    private Veterinario veterinario;

    // Campos de Data
    @CreationTimestamp // Gera a data automaticamente no banco
    @Column(updatable = false)
    private LocalDateTime dataCriacao;

    @Column(nullable = false)
    private LocalDateTime dataPrevista;

    private LocalDateTime retornoPrevisto;

    // Detalhes da Consulta
    private String tipoConsulta; // Ex: Rotina, Emergência, Cirurgia

    @Column(length = 1000)
    private String descricaoSintomas;

    @Column(length = 1000)
    private String diagnostico;

    private String status; // Ex: AGENDADA, REALIZADA, CANCELADA

    // Construtor vazio exigido pelo JPA
    public Consulta() {}


}