package br.com.challenge.models;

import jakarta.persistence.*;
import lombok.Setter;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_pets")
@Getter// LOMBOK ESTA CRIANDO O GETTER
@Setter// LOMBOK ESTA CRIANDO O SETTER

public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Tutor responsavel;

    private String nome;
    private String especie;
    private String raca;
    private LocalDate dataNascimento;
    private Double peso;
    private Double altura;
    private String sexo; // M ou F
    private String porte;
    private String statusSaude;
    private String alergias;
    private String castrado; // S ou N
    private String corPelagem;
    private String microchip;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dataCadastro;



    public Pet() {
    }
}
