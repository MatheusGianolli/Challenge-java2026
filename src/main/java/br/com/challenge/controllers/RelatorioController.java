package br.com.challenge.controllers;

import br.com.challenge.dtos.ResumoConsultaDTO;
import br.com.challenge.repositories.PkgClyvoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
@Tag(
        name = "Relatórios e Exportação",
        description = "Endpoints que executam as rotinas do package PL/SQL PKG_CLYVO"
)
public class RelatorioController {

    private final PkgClyvoRepository pkgClyvo;

    public RelatorioController(PkgClyvoRepository pkgClyvo) {
        this.pkgClyvo = pkgClyvo;
    }

    @Operation(
            summary = "Faturamento das consultas realizadas",
            description = "Chama a procedure pkg_clyvo.resumo_consultas: total por veterinário e tipo, "
                    + "subtotal por veterinário e total geral. Datas no formato AAAA-MM-DD."
    )
    @GetMapping("/resumo-consultas")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ResumoConsultaDTO>> resumoConsultas(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate inicio,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fim
    ) {
        return ResponseEntity.ok(pkgClyvo.resumoConsultas(inicio, fim));
    }

    @Operation(
            summary = "Exporta uma coleção em JSON para o MongoDB",
            description = "Chama a função pkg_clyvo.exportar_colecao_json e devolve o arquivo "
                    + "pronto para: mongoimport --jsonArray"
    )
    @GetMapping("/exportacao/{colecao}")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportarColecao(
            @Parameter(description = "TUTORES, VETERINARIOS ou CLINICAS")
            @PathVariable String colecao
    ) {
        String json = pkgClyvo.exportarColecaoJson(colecao);
        String nomeArquivo = colecao.trim().toLowerCase() + ".json";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nomeArquivo).build().toString())
                .body(json.getBytes(StandardCharsets.UTF_8));
    }
}
