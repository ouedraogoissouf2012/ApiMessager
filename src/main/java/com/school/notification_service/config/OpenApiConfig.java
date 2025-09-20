package com.school.notification_service.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

	
	@Value("${server.port:8080}")
    private String serverPort;
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                    new Server().url("http://localhost:" + serverPort).description("Serveur de développement"),
                    new Server().url("https://api.ecole.fr").description("Serveur de production")
                ))
                .info(new Info()
                    .title("API Notification École")
                    .description("API pour la gestion des notifications aux parents par email et WhatsApp")
                    .version("1.0.0")
                    .contact(new Contact()
                        .name("Équipe Développement")
                        .email("dev@ecole.fr")
                        .url("https://ecole.fr"))
                    .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT")));
    }
}
