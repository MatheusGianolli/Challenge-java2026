package br.com.challenge.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_consultas")
@Getter
@Setter
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tutor responsável pela consulta
    @ManyToOne
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Tutor responsavel;

    // Pet que será atendido
    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    // Clínica onde ocorrerá o atendimento
    @ManyToOne
    @JoinColumn(name = "clinica_id", nullable = false)
    private Clinica clinica;

    // Veterinário responsável pelo atendimento
    @ManyToOne
    @JoinColumn(name = "veterinario_id", nullable = false)
    private Veterinario veterinario;

    // Data em que o registro da consulta foi criado
    @CreationTimestamp
    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime dataCriacao;

    // Data e horário agendados
    @Column(name = "data_prevista", nullable = false)
    private LocalDateTime dataPrevista;

    // Data prevista para retorno, quando aplicável
    @Column(name = "retorno_previsto")
    private LocalDateTime retornoPrevisto;

    // Tipo: ROTINA, EMERGENCIA, CIRURGIA etc.
    @Column(name = "tipo_consulta")
    private String tipoConsulta;

    @Column(name = "descricao_sintomas", length = 1000)
    private String descricaoSintomas;

    @Column(length = 1000)
    private String diagnostico;

    // AGENDADA, REALIZADA ou CANCELADA
    @Column(name = "status")
    private String status;

    // Construtor exigido pelo JPA
    public Consulta() {
    }
}