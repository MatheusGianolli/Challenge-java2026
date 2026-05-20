package br.com.challenge.services;

import br.com.challenge.dtos.VeterinarioDTO;
import br.com.challenge.exceptions.ResourceNotFoundException;
import br.com.challenge.models.Veterinario;
import br.com.challenge.repositories.VeterinarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VeterinarioService {

    @Autowired private VeterinarioRepository repository;

    public Page<Veterinario> listarTodos(Pageable pageable) {
        return repository.findAll(pageable);
    }


    public Veterinario buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinário não encontrado com o ID: " + id));
    }

    public Veterinario cadastrar(VeterinarioDTO.Request dto) {
        Veterinario vet = new Veterinario();
        vet.setNome(dto.nome());
        vet.setCrmv(dto.crmv());
        vet.setEspecialidade(dto.especialidade());
        vet.setTipoAtuacao(dto.tipoAtuacao());
        vet.setEmail(dto.email());
        vet.setTelefone(dto.telefone());

        vet.setStatus("ATIVO"); // Cadastra como ativo por padrão
        vet.setDataUltimoAcesso(LocalDateTime.now()); // Marca o momento da criação

        return repository.save(vet);
    }

    public Veterinario atualizar(Long id, VeterinarioDTO.Request dto) {
        Veterinario vet = buscarPorId(id);

        vet.setNome(dto.nome());
        vet.setCrmv(dto.crmv());
        vet.setEspecialidade(dto.especialidade());
        vet.setTipoAtuacao(dto.tipoAtuacao());
        vet.setEmail(dto.email());
        vet.setTelefone(dto.telefone());

        return repository.save(vet);
    }


    public void excluir(Long id) {
        Veterinario vet = buscarPorId(id);
        repository.delete(vet);
    }
}