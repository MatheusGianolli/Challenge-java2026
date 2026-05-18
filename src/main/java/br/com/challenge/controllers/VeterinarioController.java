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

    @Operation(summary = "Lista todos os veterinários com paginação")
    @GetMapping
    public ResponseEntity<Page<VeterinarioDTO.Response>> listar(
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        Page<Veterinario> vets = service.listarTodos(pageable);
        return ResponseEntity.ok(vets.map(VeterinarioDTO.Response::new));
    }

    @Operation(summary = "Cadastra um novo veterinário")
    @PostMapping
    public ResponseEntity<VeterinarioDTO.Response> cadastrar(@RequestBody @Valid VeterinarioDTO.Request dto) {
        Veterinario novoVet = service.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new VeterinarioDTO.Response(novoVet));
    }
}
