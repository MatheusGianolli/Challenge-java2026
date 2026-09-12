package br.com.challenge.controllers;

import br.com.challenge.dtos.VeterinarioDTO;
import br.com.challenge.models.Veterinario;
import br.com.challenge.services.VeterinarioService;
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
public class VeterinarioController {

    @Autowired
    private VeterinarioService service;

    @GetMapping
    public ResponseEntity<Page<VeterinarioDTO.Response>> listarTodos(
            @PageableDefault(size = 10, sort = "nome") Pageable pageable,
            @RequestParam(required = false) String especialidade
    ) {

        Page<VeterinarioDTO.Response> resposta = service
                .listarTodos(pageable, especialidade)
                .map(VeterinarioDTO.Response::new);

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VeterinarioDTO.Response> buscarPorId(
            @PathVariable Long id
    ) {

        Veterinario veterinario = service.buscarPorId(id);

        return ResponseEntity.ok(
                new VeterinarioDTO.Response(veterinario)
        );
    }

    @PostMapping
    public ResponseEntity<VeterinarioDTO.Response> cadastrar(
            @Valid @RequestBody VeterinarioDTO.Request dto
    ) {

        Veterinario veterinario = service.cadastrar(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new VeterinarioDTO.Response(veterinario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VeterinarioDTO.Response> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody VeterinarioDTO.Request dto
    ) {

        Veterinario veterinario = service.atualizar(id, dto);

        return ResponseEntity.ok(
                new VeterinarioDTO.Response(veterinario)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<VeterinarioDTO.Response> alterarStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {

        Veterinario veterinario = service.alterarStatus(id, status);

        return ResponseEntity.ok(
                new VeterinarioDTO.Response(veterinario)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id
    ) {

        service.excluir(id);

        return ResponseEntity.noContent().build();
    }
}