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

    @Cacheable(value = "clinicas")
    public Page<Clinica> listarTodas(Pageable pageable) {
        return repository.findByStatus("ATIVO", pageable);
    }

    @Cacheable(value = "clinicas")
    public Page<Clinica> buscarPorNome(String nome, Pageable pageable) {
        return repository.findByStatusAndNomeContainingIgnoreCase(
                "ATIVO",
                nome,
                pageable
        );
    }

    @Cacheable(value = "clinicas")
    public Page<Clinica> buscarPorCidade(String cidade, Pageable pageable) {
        return repository.findByStatusAndCidadeContainingIgnoreCase(
                "ATIVO",
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

    @CacheEvict(value = "clinicas", allEntries = true)
    public void excluir(Long id) {

        Clinica clinica = buscarPorId(id);

        // Exclusão lógica: mantém o registro no banco.
        clinica.setStatus("INATIVO");

        repository.save(clinica);
    }
}