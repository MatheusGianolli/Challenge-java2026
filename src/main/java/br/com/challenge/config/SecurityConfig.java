package br.com.challenge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder passwordEncoder
    ) {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        UserDetails veterinario = User.builder()
                .username("veterinario")
                .password(passwordEncoder.encode("vet123"))
                .roles("VETERINARIO")
                .build();

        return new InMemoryUserDetailsManager(
                admin,
                veterinario
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> {})

                .authorizeHttpRequests(auth -> auth

                        // Permite requisições de preflight do navegador
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // Permite acesso à documentação da API
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // Admin e veterinário podem consultar veterinários
                        // para preencher o formulário de consultas
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/veterinarios/**"
                        )
                        .hasAnyRole("ADMIN", "VETERINARIO")

                        // Cadastro, edição, ativação, desativação e exclusão
                        // de veterinários continuam exclusivos do admin
                        .requestMatchers(
                                "/api/veterinarios/**"
                        )
                        .hasRole("ADMIN")

                        // Exclusões das demais entidades são exclusivas do admin
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/**"
                        )
                        .hasRole("ADMIN")

                        // Demais operações da API permitidas aos dois perfis
                        .requestMatchers(
                                "/api/**"
                        )
                        .hasAnyRole("ADMIN", "VETERINARIO")

                        .anyRequest()
                        .authenticated()
                )

                .httpBasic(httpBasic -> {})

                .formLogin(form -> form.disable());

        return http.build();
    }
}