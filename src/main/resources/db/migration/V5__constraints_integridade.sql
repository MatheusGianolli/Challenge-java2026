package br.com.challenge.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Trata os erros de Bean Validation (ex: quando esquece de mandar o ID do Pet)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Trata os erros de Regra de Negócio (ex: choque de horário do veterinário)
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessRule(BusinessRuleException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // Trata os erros de quando não acha algo no banco de dados
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // Trata violações das constraints do banco (V3 e V5):
    // dados repetidos, campos obrigatórios e exclusões com registros vinculados
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String detalhe = String.valueOf(ex.getMostSpecificCause().getMessage()).toUpperCase();

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", mensagemDaConstraint(detalhe));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    private String mensagemDaConstraint(String detalhe) {
        for (Map.Entry<String, String> regra : MENSAGENS_CONSTRAINTS.entrySet()) {
            if (detalhe.contains(regra.getKey())) {
                return regra.getValue();
            }
        }
        if (detalhe.contains("ORA-02292")) {
            return "Não é possível excluir: existem registros vinculados a este cadastro.";
        }
        if (detalhe.contains("ORA-01400")) {
            return "Um campo obrigatório não foi informado.";
        }
        return "Os dados informados violam uma regra de integridade do banco.";
    }

    // Nome da constraint no Oracle -> mensagem para o usuário
    private static final Map<String, String> MENSAGENS_CONSTRAINTS = new LinkedHashMap<>();
    static {
        MENSAGENS_CONSTRAINTS.put("UK_TUTORES_DOCUMENTO", "Já existe um responsável cadastrado com este documento.");
        MENSAGENS_CONSTRAINTS.put("UK_VETERINARIOS_CRMV", "Já existe um veterinário cadastrado com este CRMV.");
        MENSAGENS_CONSTRAINTS.put("UK_CLINICAS_CNPJ", "Já existe uma clínica cadastrada com este CNPJ.");
        MENSAGENS_CONSTRAINTS.put("UK_PETS_MICROCHIP", "Já existe um pet cadastrado com este microchip.");
        MENSAGENS_CONSTRAINTS.put("CK_PETS_SEXO", "O sexo do pet deve ser M ou F.");
        MENSAGENS_CONSTRAINTS.put("CK_PETS_PESO", "O peso do pet não pode ser negativo.");
        MENSAGENS_CONSTRAINTS.put("CK_PETS_ALTURA", "A altura do pet não pode ser negativa.");
        MENSAGENS_CONSTRAINTS.put("CK_CONSULTAS_RETORNO", "A data de retorno deve ser posterior à data da consulta.");
        MENSAGENS_CONSTRAINTS.put("CK_CONSULTAS_VALOR", "O valor da consulta não pode ser negativo.");
        MENSAGENS_CONSTRAINTS.put("CK_CONSULTAS_STATUS", "Status da consulta inválido (AGENDADA, REALIZADA ou CANCELADA).");
        MENSAGENS_CONSTRAINTS.put("CK_TUTORES_STATUS", "Status inválido (ATIVO ou INATIVO).");
        MENSAGENS_CONSTRAINTS.put("CK_VETERINARIOS_STATUS", "Status inválido (ATIVO ou INATIVO).");
        MENSAGENS_CONSTRAINTS.put("CK_CLINICAS_STATUS", "Status inválido (ATIVO ou INATIVO).");
    }
}