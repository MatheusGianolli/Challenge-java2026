package br.com.challenge.services;

import br.com.challenge.dtos.ConsultaResponseDTO;
import br.com.challenge.dtos.PetDTO;
import br.com.challenge.exceptions.ResourceNotFoundException;
import br.com.challenge.models.Consulta;
import br.com.challenge.models.Pet;
import br.com.challenge.models.Tutor;
import br.com.challenge.repositories.ConsultaRepository;
import br.com.challenge.repositories.PetRepository;
import br.com.challenge.repositories.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PetService {

    private static final String REALIZADA = "REALIZADA";
    private static final String AGENDADA = "AGENDADA";

    private final PetRepository petRepository;
    private final TutorRepository tutorRepository;
    private final ConsultaRepository consultaRepository;

    public Page<Pet> listarTodos(Pageable pageable) {
        return petRepository.findAll(pageable);
    }

    // Busca um pet específico pelo ID
    public Pet buscarPorId(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado com o ID: " + id));
    }

    public Pet cadastrar(PetDTO.Request dto) {
        Pet pet = new Pet();
        pet.setResponsavel(buscarResponsavel(dto.responsavelId()));
        preencherDados(pet, dto);
        return petRepository.save(pet);
    }

    public Pet atualizar(Long id, PetDTO.Request dto) {
        Pet pet = buscarPorId(id);

        // Se o responsável mudou, busca o novo tutor
        if (!pet.getResponsavel().getId().equals(dto.responsavelId())) {
            pet.setResponsavel(buscarResponsavel(dto.responsavelId()));
        }

        preencherDados(pet, dto);
        return petRepository.save(pet);
    }

    // Exclui o pet do banco de dados
    public void excluir(Long id) {
        Pet pet = buscarPorId(id);
        petRepository.delete(pet);
    }

    /**
     * Prontuário do pet: reúne os dados de saúde e todo o histórico de
     * consultas, diagnósticos e retornos — o "cuidado contínuo" da Clyvo.
     */
    @Transactional(readOnly = true)
    public PetDTO.Historico historico(Long id) {
        Pet pet = buscarPorId(id);
        List<Consulta> consultas = consultaRepository.findByPetIdOrderByDataPrevistaDesc(id);
        LocalDateTime agora = LocalDateTime.now();

        long realizadas = consultas.stream()
                .filter(c -> REALIZADA.equals(c.getStatus()))
                .count();

        LocalDateTime ultimoAtendimento = consultas.stream()
                .filter(c -> REALIZADA.equals(c.getStatus()))
                .map(Consulta::getDataPrevista)
                .max(Comparator.naturalOrder())
                .orElse(null);

        LocalDateTime proximaConsulta = consultas.stream()
                .filter(c -> AGENDADA.equals(c.getStatus()))
                .map(Consulta::getDataPrevista)
                .filter(data -> data.isAfter(agora))
                .min(Comparator.naturalOrder())
                .orElse(null);

        LocalDateTime proximoRetorno = consultas.stream()
                .map(Consulta::getRetornoPrevisto)
                .filter(Objects::nonNull)
                .filter(data -> data.isAfter(agora))
                .min(Comparator.naturalOrder())
                .orElse(null);

        Tutor responsavel = pet.getResponsavel();

        return new PetDTO.Historico(
                new PetDTO.Response(pet),
                responsavel != null ? responsavel.getTelefone() : null,
                responsavel != null ? responsavel.getEmail() : null,
                consultas.size(),
                realizadas,
                ultimoAtendimento,
                proximaConsulta,
                proximoRetorno,
                consultas.stream().map(ConsultaResponseDTO::new).toList()
        );
    }

    private Tutor buscarResponsavel(Long responsavelId) {
        return tutorRepository.findById(responsavelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Responsável não encontrado com o ID: " + responsavelId));
    }

    // Copia os dados do DTO para a entidade (usado no cadastro e na atualização)
    private void preencherDados(Pet pet, PetDTO.Request dto) {
        pet.setNome(dto.nome());
        pet.setEspecie(dto.especie());
        pet.setRaca(dto.raca());
        pet.setDataNascimento(dto.dataNascimento());
        pet.setPeso(dto.peso());
        pet.setAltura(dto.altura());
        pet.setSexo(dto.sexo());
        pet.setPorte(dto.porte());
        pet.setStatusSaude(dto.statusSaude());
        pet.setAlergias(dto.alergias());
        pet.setCastrado(dto.castrado());
        pet.setCorPelagem(dto.corPelagem());
        pet.setMicrochip(dto.microchip());
    }
}
