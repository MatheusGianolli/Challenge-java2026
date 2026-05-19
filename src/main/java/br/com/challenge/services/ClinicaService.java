
package br.com.challenge.services;

import br.com.challenge.dtos.ClinicaDTO;
import br.com.challenge.models.Clinica;
import br.com.challenge.repositories.ClinicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ClinicaService {

    @Autowired private ClinicaRepository repository;

    // --- MÉTODOS DE LEITURA (READ) E BUSCA ---
    public Page<Clinica> listarTodas(Pageable pageable) {
        return repository.findAll(pageable);
    }

    // Método para o requisito de busca por parâmetro
    public Page<Clinica> buscarPorNome(String nome, Pageable pageable) {
        return repository.findByNomeContainingIgnoreCase(nome, pageable);
    }

    public Clinica buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clínica não encontrada com o ID: " + id));
    }

    // --- MÉTODO DE CRIAÇÃO (CREATE) ---
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

        clinica.setStatus("ATIVO"); // Status padrão ao criar

        return repository.save(clinica);
    }

    // --- MÉTODO DE ATUALIZAÇÃO (UPDATE) ---
    public Clinica atualizar(Long id, ClinicaDTO.Request dto) {
        Clinica clinica = buscarPorId(id); // Garante que a clínica existe

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

    // --- MÉTODO DE EXCLUSÃO (DELETE) ---
    public void excluir(Long id) {
        Clinica clinica = buscarPorId(id); // Verifica se existe antes de excluir
        repository.delete(clinica);
    }
}
