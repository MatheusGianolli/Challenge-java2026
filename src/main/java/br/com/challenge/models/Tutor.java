package br.com.challenge.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tb_tutores")
@Getter
@Setter
public class Tutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String tipoDocumento; // CPF ou CNPJ
    private String numeroDocumento;
    private LocalDate dataNascimento;
    private String email;
    private String telefone;
    private String tipoResponsavel; // tutor, ONG, abrigo
    private String preferenciaContato; // WhatsApp, telefone, e-mail
    private String recebeNotificacao; // S ou N
    private String autorizaMonitoramento; // S ou N

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

    private LocalDateTime dataUltimoAcesso;

    // Relacionamento: Um Tutor pode ter vários Pets
    @OneToMany(mappedBy = "responsavel", cascade = CascadeType.ALL)
    private List<Pet> pets;

    public Tutor() {}
}
