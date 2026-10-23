package br.com.challenge.dtos;

import java.math.BigDecimal;

/**
 * Linha do relatório gerado pela procedure pkg_clyvo.resumo_consultas.
 *
 * tipoLinha:
 *  - DETALHE     -> total de um veterinário em um tipo de consulta
 *  - SUBTOTAL    -> total do veterinário (todos os tipos)
 *  - TOTAL_GERAL -> total de todas as consultas realizadas
 */
public record ResumoConsultaDTO(
        String tipoLinha,
        Long veterinarioId,
        String veterinario,
        String tipoConsulta,
        Long quantidade,
        BigDecimal valorTotal
) {
}