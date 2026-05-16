package br.com.challenge.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_consultas")
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

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Tutor getResponsavel() { return responsavel; }
    public void setResponsavel(Tutor responsavel) { this.responsavel = responsavel; }
    public Pet getPet() { return pet; }
    public void setPet(Pet pet) { this.pet = pet; }
    public Clinica getClinica() { return clinica; }
    public void setClinica(Clinica clinica) { this.clinica = clinica; }
    public Veterinario getVeterinario() { return veterinario; }
    public void setVeterinario(Veterinario veterinario) { this.veterinario = veterinario; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public LocalDateTime getDataPrevista() { return dataPrevista; }
    public void setDataPrevista(LocalDateTime dataPrevista) { this.dataPrevista = dataPrevista; }
    public LocalDateTime getRetornoPrevisto() { return retornoPrevisto; }
    public void setRetornoPrevisto(LocalDateTime retornoPrevisto) { this.retornoPrevisto = retornoPrevisto; }
    public String getTipoConsulta() { return tipoConsulta; }
    public void setTipoConsulta(String tipoConsulta) { this.tipoConsulta = tipoConsulta; }
    public String getDescricaoSintomas() { return descricaoSintomas; }
    public void setDescricaoSintomas(String descricaoSintomas) { this.descricaoSintomas = descricaoSintomas; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}