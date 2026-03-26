package br.edu.fatec.startiot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("START IoT API")
                        .description("Sistema de cronometragem e gestão para corridas de carrinho de rolimã — FATEC")
                        .version("v1.0 — Fase 3")
                        .contact(new Contact()
                                .name("FATEC START IoT")
                                .email("startiot@fatec.br")));
    }
}
