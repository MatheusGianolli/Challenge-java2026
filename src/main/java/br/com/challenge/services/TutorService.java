package br.com.challenge.services;

import br.com.challenge.dtos.TutorDTO;
import br.com.challenge.exceptions.ResourceNotFoundException;
import br.com.challenge.models.Tutor;
import br.com.challenge.repositories.TutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TutorService {

    @Autowired private TutorRepository repository;

    public Page<Tutor> listarTodos(Pageable pageable) {
        return repository.findAll(pageable);
    }

    //  Buscar um tutor específico pelo ID
    public Tutor buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado com o ID: " + id));
    }

    public Tutor cadastrar(TutorDTO.Request dto) {
        Tutor tutor = new Tutor();
        tutor.setNome(dto.nome());
        tutor.setTipoDocumento(dto.tipoDocumento());
        tutor.setNumeroDocumento(dto.numeroDocumento());
        tutor.setDataNascimento(dto.dataNascimento());
        tutor.setEmail(dto.email());
        tutor.setTelefone(dto.telefone());
        tutor.setTipoResponsavel(dto.tipoResponsavel());
        tutor.setPreferenciaContato(dto.preferenciaContato());
        tutor.setRecebeNotificacao(dto.recebeNotificacao());
        tutor.setAutorizaMonitoramento(dto.autorizaMonitoramento());
        tutor.setLogradouro(dto.logradouro());
        tutor.setNumeroEndereco(dto.numeroEndereco());
        tutor.setBairro(dto.bairro());
        tutor.setCidade(dto.cidade());
        tutor.setEstado(dto.estado());
        tutor.setCep(dto.cep());

        tutor.setStatus("ATIVO"); // Cadastra como ativo por padrão
        tutor.setDataUltimoAcesso(LocalDateTime.now()); // Marca o momento da criação

        return repository.save(tutor);
    }

    //  Atualizar os dados de um tutor existente
    public Tutor atualizar(Long id, TutorDTO.Request dto) {
        Tutor tutor = buscarPorId(id); // Usa o método novo para achar o tutor

        tutor.setNome(dto.nome());
        tutor.setTipoDocumento(dto.tipoDocumento());
        tutor.setNumeroDocumento(dto.numeroDocumento());
        tutor.setDataNascimento(dto.dataNascimento());
        tutor.setEmail(dto.email());
        tutor.setTelefone(dto.telefone());
        tutor.setTipoResponsavel(dto.tipoResponsavel());
        tutor.setPreferenciaContato(dto.preferenciaContato());
        tutor.setRecebeNotificacao(dto.recebeNotificacao());
        tutor.setAutorizaMonitoramento(dto.autorizaMonitoramento());
        tutor.setLogradouro(dto.logradouro());
        tutor.setNumeroEndereco(dto.numeroEndereco());
        tutor.setBairro(dto.bairro());
        tutor.setCidade(dto.cidade());
        tutor.setEstado(dto.estado());
        tutor.setCep(dto.cep());


        return repository.save(tutor);
    }


    public void excluir(Long id) {
        Tutor tutor = buscarPorId(id);
        repository.delete(tutor);
    }
}
