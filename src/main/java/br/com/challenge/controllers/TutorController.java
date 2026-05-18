package br.com.challenge.controllers;

import br.com.challenge.dtos.TutorDTO;
import br.com.challenge.models.Tutor;
import br.com.challenge.services.TutorService;
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
@RequestMapping("/api/tutores")
@Tag(name = "Tutores", description = "Endpoints para gerenciamento de Tutores")
public class TutorController {

    @Autowired private TutorService service;

    @Operation(summary = "Lista todos os tutores com paginação")
    @GetMapping
    public ResponseEntity<Page<TutorDTO.Response>> listar(
            @PageableDefault(size = 10, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Tutor> tutores = service.listarTodos(pageable);
        return ResponseEntity.ok(tutores.map(TutorDTO.Response::new));
    }

    @Operation(summary = "Cadastra um novo tutor")
    @PostMapping
    public ResponseEntity<TutorDTO.Response> cadastrar(@RequestBody @Valid TutorDTO.Request dto) {
        Tutor novoTutor = service.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new TutorDTO.Response(novoTutor));
    }
}
