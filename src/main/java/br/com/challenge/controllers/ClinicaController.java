package br.com.challenge.controllers;

import br.com.challenge.dtos.ClinicaDTO;
import br.com.challenge.models.Clinica;
import br.com.challenge.services.ClinicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clinicas")
@Tag(name = "Clínicas", description = "Endpoints para gerenciamento de Clínicas")
public class ClinicaController {

    @Autowired private ClinicaService service;

    // REQUISITO DA SPRINT: Uso de Cache para otimizar requisições
    @Cacheable(value = "clinicas")
    @Operation(summary = "Lista todas as clínicas com paginação (Otimizado com Cache)")
    @GetMapping
    public ResponseEntity<Page<ClinicaDTO.Response>> listar(
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        Page<Clinica> clinicas = service.listarTodas(pageable);
        return ResponseEntity.ok(clinicas.map(ClinicaDTO.Response::new));
    }

    // REQUISITO DA SPRINT: Busca com parâmetros
    @Operation(summary = "Busca uma clínica pelo nome")
    @GetMapping("/buscar")
    public ResponseEntity<Page<ClinicaDTO.Response>> buscarPorNome(
            @RequestParam String nome,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        Page<Clinica> clinicas = service.buscarPorNome(nome, pageable);
        return ResponseEntity.ok(clinicas.map(ClinicaDTO.Response::new));
    }

    // Limpa o cache sempre que uma nova clínica é cadastrada
    @CacheEvict(value = "clinicas", allEntries = true)
    @Operation(summary = "Cadastra uma nova clínica")
    @PostMapping
    public ResponseEntity<ClinicaDTO.Response> cadastrar(@RequestBody @Valid ClinicaDTO.Request dto) {
        Clinica novaClinica = service.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ClinicaDTO.Response(novaClinica));
    }
}
