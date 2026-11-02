package br.com.challenge.util;

import br.com.challenge.exceptions.BusinessRuleException;

/**
 * Regras do status de cadastros (clínicas, veterinários e tutores).
 * Centraliza a validação para não repetir a mesma lógica em cada service.
 */
public final class StatusCadastro {

    public static final String ATIVO = "ATIVO";
    public static final String INATIVO = "INATIVO";

    private StatusCadastro() {
    }

    /**
     * Normaliza e valida o status informado.
     *
     * @return o status em maiúsculas (ATIVO ou INATIVO)
     * @throws BusinessRuleException quando vazio ou diferente de ATIVO/INATIVO
     */
    public static String validar(String status) {
        if (status == null || status.isBlank()) {
            throw new BusinessRuleException("O status deve ser informado.");
        }

        String normalizado = status.trim().toUpperCase();

        if (!ATIVO.equals(normalizado) && !INATIVO.equals(normalizado)) {
            throw new BusinessRuleException("Status inválido. Utilize ATIVO ou INATIVO.");
        }

        return normalizado;
    }

    public static boolean estaAtivo(String status) {
        return ATIVO.equalsIgnoreCase(status);
    }
}
