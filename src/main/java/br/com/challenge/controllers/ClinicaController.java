package br.com.challenge.controllers;

import br.com.challenge.dtos.ClinicaDTO;
import br.com.challenge.models.Clinica;
import br.com.challenge.services.ClinicaService;
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
@RequestMapping("/api/clinicas")
@Tag(name = "Clínicas", description = "Endpoints para gerenciamento de clínicas")
public class ClinicaController {

    @Autowired
    private ClinicaService service;

    // Lista clínicas ativas com paginação.
    @Operation(summary = "Lista todas as clínicas ativas com paginação")
    @GetMapping
    public ResponseEntity<Page<ClinicaDTO.Response>> listar(
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {

        Page<Clinica> clinicas = service.listarTodas(pageable);

        return ResponseEntity.ok(
                clinicas.map(ClinicaDTO.Response::new)
        );
    }

    // Busca clínicas ativas pelo nome.
    @Operation(summary = "Busca clínicas ativas pelo nome")
    @GetMapping("/buscar")
    public ResponseEntity<Page<ClinicaDTO.Response>> buscarPorNome(
            @RequestParam String nome,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {

        Page<Clinica> clinicas = service.buscarPorNome(nome, pageable);

        return ResponseEntity.ok(
                clinicas.map(ClinicaDTO.Response::new)
        );
    }

    // Busca clínicas ativas pela cidade.
    @Operation(summary = "Busca clínicas ativas pela cidade")
    @GetMapping("/buscar/cidade")
    public ResponseEntity<Page<ClinicaDTO.Response>> buscarPorCidade(
            @RequestParam String cidade,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {

        Page<Clinica> clinicas = service.buscarPorCidade(cidade, pageable);

        return ResponseEntity.ok(
                clinicas.map(ClinicaDTO.Response::new)
        );
    }

    // Busca uma clínica pelo ID, ativa ou inativa.
    @Operation(summary = "Busca uma clínica pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<ClinicaDTO.Response> buscarPorId(
            @PathVariable Long id) {

        Clinica clinica = service.buscarPorId(id);

        return ResponseEntity.ok(
                new ClinicaDTO.Response(clinica)
        );
    }

    // Cadastra uma nova clínica.
    @Operation(summary = "Cadastra uma nova clínica")
    @PostMapping
    public ResponseEntity<ClinicaDTO.Response> cadastrar(
            @RequestBody @Valid ClinicaDTO.Request dto) {

        Clinica novaClinica = service.cadastrar(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ClinicaDTO.Response(novaClinica));
    }

    // Atualiza uma clínica existente.
    @Operation(summary = "Atualiza os dados de uma clínica existente")
    @PutMapping("/{id}")
    public ResponseEntity<ClinicaDTO.Response> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid ClinicaDTO.Request dto) {

        Clinica clinicaAtualizada = service.atualizar(id, dto);

        return ResponseEntity.ok(
                new ClinicaDTO.Response(clinicaAtualizada)
        );
    }

    // Exclusão lógica.
    @Operation(summary = "Desativa uma clínica")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        service.excluir(id);

        return ResponseEntity.noContent().build();
    }
}