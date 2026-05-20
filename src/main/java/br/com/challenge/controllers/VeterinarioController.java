package br.com.challenge.controllers;

import br.com.challenge.dtos.VeterinarioDTO;
import br.com.challenge.models.Veterinario;
import br.com.challenge.services.VeterinarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/veterinarios")
@Tag(name = "Veterinários", description = "Endpoints para gerenciamento de Veterinários")
public class VeterinarioController {

    @Autowired private VeterinarioService service;

    @Operation(summary = "Lista todos os veterinários com paginação e filtro opcional por especialidade")
    @GetMapping
    public ResponseEntity<Page<VeterinarioDTO.Response>> listar(
            @RequestParam(required = false) String especialidade, // <-- ADICIONADO AQUI
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {


        Page<Veterinario> vets = service.listarTodos(pageable, especialidade);
        return ResponseEntity.ok(vets.map(VeterinarioDTO.Response::new));
    }

    @Operation(summary = "Busca um veterinário pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<VeterinarioDTO.Response> detalhar(@PathVariable Long id) {
        Veterinario vet = service.buscarPorId(id);
        return ResponseEntity.ok(new VeterinarioDTO.Response(vet));
    }

    @Operation(summary = "Cadastra um novo veterinário")
    @PostMapping
    public ResponseEntity<VeterinarioDTO.Response> cadastrar(@RequestBody @Valid VeterinarioDTO.Request dto) {
        Veterinario novoVet = service.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new VeterinarioDTO.Response(novoVet));
    }

    @Operation(summary = "Atualiza os dados de um veterinário existente")
    @PutMapping("/{id}")
    public ResponseEntity<VeterinarioDTO.Response> atualizar(@PathVariable Long id, @RequestBody @Valid VeterinarioDTO.Request dto) {
        Veterinario vetAtualizado = service.atualizar(id, dto);
        return ResponseEntity.ok(new VeterinarioDTO.Response(vetAtualizado));
    }

    @Operation(summary = "Exclui um veterinário logicamente")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
