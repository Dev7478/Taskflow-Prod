package com.jbs.tfv3.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition (
		info=@Info (
				title = "Task-Flow Management",
				description = "Task-Flow Management System",
				contact = @Contact (
						name = "Jayanta B. Sen",
						email = "jayanta.b.sen@yopmail.com"
				),
				version = "1.0.0"
		),
		servers = {
				@Server (
					description = "DEV",
					url = "http://localhost:8080"
				),
				@Server (
					description = "UAT",
					url = "http://localhost:8081"
				),
				@Server (
					description = "PROD",
					url = "http://localhost:8082"
				)
		}
)

public class OpenAPIConfig {
	@Bean
    public OpenAPI customizeOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
