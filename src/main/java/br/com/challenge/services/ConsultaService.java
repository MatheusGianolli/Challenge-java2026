package br.com.challenge.services;

import br.com.challenge.dtos.ConsultaRequestDTO;
import br.com.challenge.exceptions.BusinessRuleException;
import br.com.challenge.exceptions.ResourceNotFoundException;
import br.com.challenge.models.Clinica;
import br.com.challenge.models.Consulta;
import br.com.challenge.models.Pet;
import br.com.challenge.models.Tutor;
import br.com.challenge.models.Veterinario;
import br.com.challenge.repositories.ClinicaRepository;
import br.com.challenge.repositories.ConsultaRepository;
import br.com.challenge.repositories.PetRepository;
import br.com.challenge.repositories.TutorRepository;
import br.com.challenge.repositories.VeterinarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsultaService {

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private ClinicaRepository clinicaRepository;

    @Autowired
    private VeterinarioRepository veterinarioRepository;

    // 1. LISTAR CONSULTAS COM PAGINAÇÃO
    public Page<Consulta> listarTodas(Pageable pageable) {
        return consultaRepository.findAll(pageable);
    }

    // 2. BUSCAR CONSULTA POR ID
    public Consulta buscarPorId(Long id) {
        return consultaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Consulta não encontrada com o ID: " + id
                        )
                );
    }

    // 3. AGENDAR CONSULTA
    public Consulta agendar(ConsultaRequestDTO dto) {

        Tutor responsavel = tutorRepository.findById(dto.responsavelId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Responsável não encontrado com o ID: " + dto.responsavelId()
                        )
                );

        Pet pet = petRepository.findById(dto.petId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pet não encontrado com o ID: " + dto.petId()
                        )
                );

        Clinica clinica = clinicaRepository.findById(dto.clinicaId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Clínica não encontrada com o ID: " + dto.clinicaId()
                        )
                );

        Veterinario vet = veterinarioRepository.findById(dto.veterinarioId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Veterinário não encontrado com o ID: " + dto.veterinarioId()
                        )
                );

        // Regra de negócio: não permitir agendamento com veterinário inativo
        if (!"ATIVO".equalsIgnoreCase(vet.getStatus())) {
            throw new BusinessRuleException(
                    "Não é possível agendar consulta com veterinário inativo."
            );
        }

        // Regra de negócio: não permitir agendamento em clínica inativa
        if (!"ATIVO".equalsIgnoreCase(clinica.getStatus())) {
            throw new BusinessRuleException(
                    "Não é possível agendar consulta em clínica inativa."
            );
        }

        // Regra de negócio: impedir conflito de horário do veterinário
        List<Consulta> conflitos =
                consultaRepository.buscarConflitoDeHorario(
                        vet.getId(),
                        dto.dataPrevista()
                );

        if (!conflitos.isEmpty()) {
            throw new BusinessRuleException(
                    "O veterinário já possui uma consulta agendada para este horário."
            );
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

        return consultaRepository.save(consulta);
    }

    // 4. REGISTRAR DIAGNÓSTICO E FINALIZAR CONSULTA
    public Consulta atualizarDiagnostico(
            Long id,
            String diagnostico,
            LocalDateTime retorno
    ) {
        Consulta consulta = buscarPorId(id);

        if ("CANCELADA".equalsIgnoreCase(consulta.getStatus())) {
            throw new BusinessRuleException(
                    "Não é possível registrar diagnóstico em uma consulta cancelada."
            );
        }

        if ("REALIZADA".equalsIgnoreCase(consulta.getStatus())) {
            throw new BusinessRuleException(
                    "Esta consulta já foi finalizada."
            );
        }

        consulta.setDiagnostico(diagnostico);
        consulta.setRetornoPrevisto(retorno);
        consulta.setStatus("REALIZADA");

        return consultaRepository.save(consulta);
    }

    // 5. CANCELAR CONSULTA
    public void cancelar(Long id) {
        Consulta consulta = buscarPorId(id);

        if ("REALIZADA".equalsIgnoreCase(consulta.getStatus())) {
            throw new BusinessRuleException(
                    "Não é possível cancelar uma consulta já realizada."
            );
        }

        if ("CANCELADA".equalsIgnoreCase(consulta.getStatus())) {
            throw new BusinessRuleException(
                    "Esta consulta já está cancelada."
            );
        }

        consulta.setStatus("CANCELADA");
        consultaRepository.save(consulta);
    }
}