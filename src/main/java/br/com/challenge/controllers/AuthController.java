package br.com.challenge.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/me")
@Tag(
        name = "Autenticação",
        description = "Dados do usuário autenticado"
)
public class AuthController {

    @Operation(
            summary = "Retorna o usuário logado e seus perfis",
            description = "Usado pela interface web para validar o login e mostrar as opções de cada perfil"
    )
    @GetMapping
    public ResponseEntity<Map<String, Object>> usuarioLogado(Authentication auth) {
        List<String> perfis = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(perfil -> perfil.replace("ROLE_", ""))
                .toList();

        return ResponseEntity.ok(Map.of(
                "usuario", auth.getName(),
                "perfis", perfis
        ));
    }
}
