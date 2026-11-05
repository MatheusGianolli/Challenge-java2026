package br.com.challenge.util;

import br.com.challenge.exceptions.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitários da regra de status dos cadastros (tutores, clínicas e veterinários).
 * Não dependem de banco: rodam no build e na pipeline de CI.
 */
class StatusCadastroTest {

    @Test
    @DisplayName("Aceita ATIVO e INATIVO")
    void aceitaStatusValidos() {
        assertEquals("ATIVO", StatusCadastro.validar("ATIVO"));
        assertEquals("INATIVO", StatusCadastro.validar("INATIVO"));
    }

    @Test
    @DisplayName("Normaliza espaços e letras minúsculas")
    void normalizaStatus() {
        assertEquals("ATIVO", StatusCadastro.validar("  ativo "));
        assertEquals("INATIVO", StatusCadastro.validar("Inativo"));
    }

    @Test
    @DisplayName("Rejeita status nulo ou vazio")
    void rejeitaStatusVazio() {
        BusinessRuleException nulo = assertThrows(BusinessRuleException.class,
                () -> StatusCadastro.validar(null));
        assertEquals("O status deve ser informado.", nulo.getMessage());

        assertThrows(BusinessRuleException.class, () -> StatusCadastro.validar("   "));
    }

    @Test
    @DisplayName("Rejeita status fora da lista")
    void rejeitaStatusInvalido() {
        BusinessRuleException erro = assertThrows(BusinessRuleException.class,
                () -> StatusCadastro.validar("BLOQUEADO"));
        assertEquals("Status inválido. Utilize ATIVO ou INATIVO.", erro.getMessage());
    }

    @Test
    @DisplayName("Identifica cadastro ativo sem diferenciar maiúsculas")
    void identificaAtivo() {
        assertTrue(StatusCadastro.estaAtivo("ATIVO"));
        assertTrue(StatusCadastro.estaAtivo("ativo"));
        assertFalse(StatusCadastro.estaAtivo("INATIVO"));
        assertFalse(StatusCadastro.estaAtivo(null));
    }
}
