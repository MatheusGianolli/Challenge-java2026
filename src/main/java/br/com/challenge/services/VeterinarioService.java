package br.com.challenge.services;

import br.com.challenge.dtos.VeterinarioDTO;
import br.com.challenge.exceptions.ResourceNotFoundException;
import br.com.challenge.models.Veterinario;
import br.com.challenge.repositories.VeterinarioRepository;
import br.com.challenge.util.StatusCadastro;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VeterinarioService {

    private final VeterinarioRepository repository;

    @Cacheable(value = "veterinarios")
    public Page<Veterinario> listarTodos(Pageable pageable, String especialidade) {
        if (especialidade != null && !especialidade.isBlank()) {
            return repository.findByEspecialidadeContainingIgnoreCase(especialidade, pageable);
        }
        return repository.findAll(pageable);
    }

    public Veterinario buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinário não encontrado com o ID: " + id));
    }

    @CacheEvict(value = "veterinarios", allEntries = true)
    public Veterinario cadastrar(VeterinarioDTO.Request dto) {
        Veterinario vet = new Veterinario();
        preencherDados(vet, dto);

        vet.setStatus(StatusCadastro.ATIVO);
        vet.setDataUltimoAcesso(LocalDateTime.now());

        return repository.save(vet);
    }

    @CacheEvict(value = "veterinarios", allEntries = true)
    public Veterinario atualizar(Long id, VeterinarioDTO.Request dto) {
        Veterinario vet = buscarPorId(id);
        preencherDados(vet, dto);
        return repository.save(vet);
    }

    @CacheEvict(value = "veterinarios", allEntries = true)
    public Veterinario alterarStatus(Long id, String status) {
        Veterinario vet = buscarPorId(id);
        vet.setStatus(StatusCadastro.validar(status));
        return repository.save(vet);
    }

    @Transactional
    @CacheEvict(value = "veterinarios", allEntries = true)
    public void excluir(Long id) {
        Veterinario vet = buscarPorId(id);
        repository.delete(vet);
    }

    // Copia os dados do DTO para a entidade (usado no cadastro e na atualização)
    private void preencherDados(Veterinario vet, VeterinarioDTO.Request dto) {
        vet.setNome(dto.nome());
        vet.setCrmv(dto.crmv());
        vet.setEspecialidade(dto.especialidade());
        vet.setTipoAtuacao(dto.tipoAtuacao());
        vet.setEmail(dto.email());
        vet.setTelefone(dto.telefone());
    }
}
