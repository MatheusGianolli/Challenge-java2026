package br.com.challenge.services;

import br.com.challenge.dtos.ConsultaRequestDTO;
import br.com.challenge.exceptions.BusinessRuleException;
import br.com.challenge.exceptions.ResourceNotFoundException;
import br.com.challenge.models.*;
import br.com.challenge.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsultaService {

    @Autowired private ConsultaRepository consultaRepository;
    @Autowired private TutorRepository tutorRepository;
    @Autowired private PetRepository petRepository;
    @Autowired private ClinicaRepository clinicaRepository;
    @Autowired private VeterinarioRepository veterinarioRepository;

    // 1. LISTAR COM PAGINAÇÃO
    public Page<Consulta> listarTodas(Pageable pageable) {
        return consultaRepository.findAll(pageable);
    }

    // 2. BUSCAR POR ID
    public Consulta buscarPorId(Long id) {
        return consultaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada com o ID: " + id));
    }

    // 3. CRIAR (AGENDAR) CONSULTA
    public Consulta agendar(ConsultaRequestDTO dto) {
        // Buscando as chaves estrangeiras no banco
        Tutor responsavel = tutorRepository.findById(dto.responsavelId())
                .orElseThrow(() -> new ResourceNotFoundException("Responsável não encontrado"));
        Pet pet = petRepository.findById(dto.petId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado"));
        Clinica clinica = clinicaRepository.findById(dto.clinicaId())
                .orElseThrow(() -> new ResourceNotFoundException("Clínica não encontrada"));
        Veterinario vet = veterinarioRepository.findById(dto.veterinarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinário não encontrado"));

        // Regra de Negócio: Evitar choque de horário para o veterinário
        List<Consulta> conflitos = consultaRepository.buscarConflitoDeHorario(vet.getId(), dto.dataPrevista());
        if (!conflitos.isEmpty()) {
            throw new BusinessRuleException("O veterinário já possui uma consulta agendada para este horário exato.");
        }

        Consulta consulta = new Consulta();
        consulta.setResponsavel(responsavel);
        consulta.setPet(pet);
        consulta.setClinica(clinica);
        consulta.setVeterinario(vet);
        consulta.setDataPrevista(dto.dataPrevista());
        consulta.setTipoConsulta(dto.tipoConsulta());
        consulta.setDescricaoSintomas(dto.descricaoSintomas());
        consulta.setStatus("AGENDADA");
        // dataCriacao é gerada automaticamente pelo @CreationTimestamp

        return consultaRepository.save(consulta);
    }

    // 4. ATUALIZAR STATUS E DIAGNÓSTICO
    public Consulta atualizarDiagnostico(Long id, String diagnostico, LocalDateTime retorno) {
        Consulta consulta = buscarPorId(id);
        consulta.setDiagnostico(diagnostico);
        consulta.setRetornoPrevisto(retorno);
        consulta.setStatus("REALIZADA");
        return consultaRepository.save(consulta);
    }

    // 5. DELETAR (CANCELAR) CONSULTA
    public void cancelar(Long id) {
        Consulta consulta = buscarPorId(id);
        consulta.setStatus("CANCELADA");
        consultaRepository.save(consulta);
    }
}
