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

    // Usando o PetDTO.Request
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
}
