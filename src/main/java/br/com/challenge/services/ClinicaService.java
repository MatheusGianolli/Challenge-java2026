package br.com.challenge.services;

import br.com.challenge.dtos.ClinicaDTO;
import br.com.challenge.exceptions.ResourceNotFoundException;
import br.com.challenge.models.Clinica;
import br.com.challenge.repositories.ClinicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ClinicaService {

    @Autowired
    private ClinicaRepository repository;

    // Lista clínicas ativas e inativas.
    @Cacheable(value = "clinicas")
    public Page<Clinica> listarTodas(Pageable pageable) {
        return repository.findAll(pageable);
    }

    // Busca clínicas ativas e inativas pelo nome.
    @Cacheable(value = "clinicas")
    public Page<Clinica> buscarPorNome(String nome, Pageable pageable) {
        return repository.findByNomeContainingIgnoreCase(
                nome,
                pageable
        );
    }

    // Busca clínicas ativas e inativas pela cidade.
    @Cacheable(value = "clinicas")
    public Page<Clinica> buscarPorCidade(String cidade, Pageable pageable) {
        return repository.findByCidadeContainingIgnoreCase(
                cidade,
                pageable
        );
    }

    public Clinica buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Clínica não encontrada com o ID: " + id
                        )
                );
    }

    @CacheEvict(value = "clinicas", allEntries = true)
    public Clinica cadastrar(ClinicaDTO.Request dto) {

        Clinica clinica = new Clinica();

        clinica.setNome(dto.nome());
        clinica.setCnpj(dto.cnpj());
        clinica.setEmail(dto.email());
        clinica.setTelefone(dto.telefone());
        clinica.setLogradouro(dto.logradouro());
        clinica.setNumeroEndereco(dto.numeroEndereco());
        clinica.setBairro(dto.bairro());
        clinica.setCidade(dto.cidade());
        clinica.setEstado(dto.estado());
        clinica.setCep(dto.cep());
        clinica.setTipoEstabelecimento(dto.tipoEstabelecimento());
        clinica.setEspecialidadePrincipal(dto.especialidadePrincipal());
        clinica.setAtendimento24h(dto.atendimento24h());
        clinica.setAtendeEmergencia(dto.atendeEmergencia());

        // Toda clínica começa ativa.
        clinica.setStatus("ATIVO");

        return repository.save(clinica);
    }

    @CacheEvict(value = "clinicas", allEntries = true)
    public Clinica atualizar(Long id, ClinicaDTO.Request dto) {

        Clinica clinica = buscarPorId(id);

        clinica.setNome(dto.nome());
        clinica.setCnpj(dto.cnpj());
        clinica.setEmail(dto.email());
        clinica.setTelefone(dto.telefone());
        clinica.setLogradouro(dto.logradouro());
        clinica.setNumeroEndereco(dto.numeroEndereco());
        clinica.setBairro(dto.bairro());
        clinica.setCidade(dto.cidade());
        clinica.setEstado(dto.estado());
        clinica.setCep(dto.cep());
        clinica.setTipoEstabelecimento(dto.tipoEstabelecimento());
        clinica.setEspecialidadePrincipal(dto.especialidadePrincipal());
        clinica.setAtendimento24h(dto.atendimento24h());
        clinica.setAtendeEmergencia(dto.atendeEmergencia());

        return repository.save(clinica);
    }

    // Altera o status sem excluir a clínica.
    @CacheEvict(value = "clinicas", allEntries = true)
    public Clinica alterarStatus(Long id, String status) {

        Clinica clinica = buscarPorId(id);

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "O status da clínica deve ser informado."
            );
        }

        String novoStatus = status.trim().toUpperCase();

        if (!novoStatus.equals("ATIVO")
                && !novoStatus.equals("INATIVO")) {

            throw new IllegalArgumentException(
                    "Status inválido. Utilize ATIVO ou INATIVO."
            );
        }

        clinica.setStatus(novoStatus);

        return repository.save(clinica);
    }

    // Desativação lógica mantida para compatibilidade.
    @CacheEvict(value = "clinicas", allEntries = true)
    public void excluir(Long id) {

        Clinica clinica = buscarPorId(id);

        clinica.setStatus("INATIVO");

        repository.save(clinica);
    }

    // Exclusão física: remove definitivamente a clínica do banco.
    @CacheEvict(value = "clinicas", allEntries = true)
    public void excluirPermanentemente(Long id) {

        Clinica clinica = buscarPorId(id);

        repository.delete(clinica);
    }
}