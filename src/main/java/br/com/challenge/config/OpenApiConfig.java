package br.com.challenge.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Plataforma Veterinária - Challenge")
                        .version("1.0.0")
                        .description("API RESTful para gerenciamento de clínica veterinária. Desenvolvido para a 1ª Sprint de Java Advanced."));
    }
}
