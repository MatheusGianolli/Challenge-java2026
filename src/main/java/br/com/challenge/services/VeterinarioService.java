package br.com.challenge.services;

import br.com.challenge.dtos.VeterinarioDTO;
import br.com.challenge.exceptions.ResourceNotFoundException;
import br.com.challenge.models.Veterinario;
import br.com.challenge.repositories.VeterinarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class VeterinarioService {

    @Autowired
    private VeterinarioRepository repository;

    @Cacheable(value = "veterinarios")
    public Page<Veterinario> listarTodos(
            Pageable pageable,
            String especialidade
    ) {
        if (especialidade != null && !especialidade.trim().isEmpty()) {
            return repository.findByEspecialidadeContainingIgnoreCase(
                    especialidade,
                    pageable
            );
        }

        return repository.findAll(pageable);
    }

    public Veterinario buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Veterinário não encontrado com o ID: " + id
                        )
                );
    }

    @CacheEvict(value = "veterinarios", allEntries = true)
    public Veterinario cadastrar(VeterinarioDTO.Request dto) {
        Veterinario vet = new Veterinario();

        vet.setNome(dto.nome());
        vet.setCrmv(dto.crmv());
        vet.setEspecialidade(dto.especialidade());
        vet.setTipoAtuacao(dto.tipoAtuacao());
        vet.setEmail(dto.email());
        vet.setTelefone(dto.telefone());
        vet.setStatus("ATIVO");
        vet.setDataUltimoAcesso(LocalDateTime.now());

        return repository.save(vet);
    }

    @CacheEvict(value = "veterinarios", allEntries = true)
    public Veterinario atualizar(
            Long id,
            VeterinarioDTO.Request dto
    ) {
        Veterinario vet = buscarPorId(id);

        vet.setNome(dto.nome());
        vet.setCrmv(dto.crmv());
        vet.setEspecialidade(dto.especialidade());
        vet.setTipoAtuacao(dto.tipoAtuacao());
        vet.setEmail(dto.email());
        vet.setTelefone(dto.telefone());

        return repository.save(vet);
    }

    @CacheEvict(value = "veterinarios", allEntries = true)
    public Veterinario alterarStatus(Long id, String status) {
        Veterinario vet = buscarPorId(id);

        String novoStatus = status.trim().toUpperCase();

        if (!novoStatus.equals("ATIVO")
                && !novoStatus.equals("INATIVO")) {
            throw new IllegalArgumentException(
                    "Status inválido. Utilize ATIVO ou INATIVO."
            );
        }

        vet.setStatus(novoStatus);

        return repository.save(vet);
    }

    @Transactional
    @CacheEvict(value = "veterinarios", allEntries = true)
    public void excluir(Long id) {
        Veterinario vet = buscarPorId(id);

        repository.delete(vet);
    }
}