package br.com.challenge.controllers;

import br.com.challenge.dtos.PetDTO;
import br.com.challenge.models.Pet;
import br.com.challenge.services.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pets")
@Tag(name = "Pets", description = "Endpoints para gerenciamento de Pets")
public class PetController {

    @Autowired private PetService service;

    @Operation(summary = "Lista todos os pets com paginação")
    @GetMapping
    public ResponseEntity<Page<PetDTO.Response>> listar(
            @PageableDefault(size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Pet> pets = service.listarTodos(pageable);
        return ResponseEntity.ok(pets.map(PetDTO.Response::new));
    }

    @Operation(summary = "Busca um pet pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<PetDTO.Response> detalhar(@PathVariable Long id) {
        Pet pet = service.buscarPorId(id);
        return ResponseEntity.ok(new PetDTO.Response(pet));
    }

    @Operation(summary = "Cadastra um novo pet")
    @PostMapping
    public ResponseEntity<PetDTO.Response> cadastrar(@RequestBody @Valid PetDTO.Request dto) {
        Pet novoPet = service.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PetDTO.Response(novoPet));
    }

    @Operation(summary = "Atualiza os dados de um pet existente")
    @PutMapping("/{id}")
    public ResponseEntity<PetDTO.Response> atualizar(@PathVariable Long id, @RequestBody @Valid PetDTO.Request dto) {
        Pet petAtualizado = service.atualizar(id, dto);
        return ResponseEntity.ok(new PetDTO.Response(petAtualizado));
    }

    @Operation(summary = "Exclui um pet logicamente")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}