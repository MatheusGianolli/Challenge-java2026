package br.com.challenge.controllers;

import br.com.challenge.dtos.ConsultaRequestDTO;
import br.com.challenge.dtos.ConsultaResponseDTO;
import br.com.challenge.models.Consulta;
import br.com.challenge.services.ConsultaService;
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
@RequestMapping("/api/consultas")
@Tag(name = "Consultas", description = "Endpoints de gerenciamento de consultas veterinárias")
public class ConsultaController {

    @Autowired
    private ConsultaService service;

    @Operation(summary = "Lista todas as consultas com paginação e ordenação")
    @GetMapping
    public ResponseEntity<Page<ConsultaResponseDTO>> listar(
            @PageableDefault(size = 10, sort = "dataPrevista", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Consulta> consultas = service.listarTodas(pageable);
        return ResponseEntity.ok(consultas.map(ConsultaResponseDTO::new)); // Converte Entidade para DTO
    }

    @Operation(summary = "Busca uma consulta pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<ConsultaResponseDTO> buscarPorId(@PathVariable Long id) {
        Consulta consulta = service.buscarPorId(id);
        return ResponseEntity.ok(new ConsultaResponseDTO(consulta));
    }

    @Operation(summary = "Cadastra (Agenda) uma nova consulta")
    @PostMapping
    public ResponseEntity<ConsultaResponseDTO> agendar(@RequestBody @Valid ConsultaRequestDTO dto) {
        Consulta novaConsulta = service.agendar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ConsultaResponseDTO(novaConsulta));
    }

    @Operation(summary = "Cancela uma consulta alterando seu status")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        service.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
