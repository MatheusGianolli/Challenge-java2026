package br.com.challenge.services;

import br.com.challenge.dtos.PetDTO;
import br.com.challenge.exceptions.ResourceNotFoundException;
import br.com.challenge.models.Pet;
import br.com.challenge.models.Tutor;
import br.com.challenge.repositories.PetRepository;
import br.com.challenge.repositories.TutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PetService {

    @Autowired private PetRepository petRepository;
    @Autowired private TutorRepository tutorRepository;

    public Page<Pet> listarTodos(Pageable pageable) {
        return petRepository.findAll(pageable);
    }

    // MÉTODO  1: Buscar um pet específico pelo ID
    public Pet buscarPorId(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado com o ID: " + id));
    }


    public Pet cadastrar(PetDTO.Request dto) {
        Tutor responsavel = tutorRepository.findById(dto.responsavelId())
                .orElseThrow(() -> new ResourceNotFoundException("Responsável não encontrado no banco de dados"));

        Pet pet = new Pet();
        pet.setResponsavel(responsavel);
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

        return petRepository.save(pet);
    }


    public Pet atualizar(Long id, PetDTO.Request dto) {
        Pet pet = buscarPorId(id); // Usa o método novo para achar o pet

        // Se por acaso o ID do tutor na requisição for diferente do atual, ele atualiza o dono
        if (!pet.getResponsavel().getId().equals(dto.responsavelId())) {
            Tutor novoResponsavel = tutorRepository.findById(dto.responsavelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Novo responsável não encontrado"));
            pet.setResponsavel(novoResponsavel);
        }

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

        return petRepository.save(pet);
    }

    // : Excluir o pet do banco de dados
    public void excluir(Long id) {
        Pet pet = buscarPorId(id);
        petRepository.delete(pet);
    }
}
