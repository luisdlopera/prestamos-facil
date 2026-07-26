package com.prestamosfacil.infrastructure.adapter.in.rest.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title(SwaggerDocs.API_TITLE)
                .version(SwaggerDocs.API_VERSION)
                .description(SwaggerDocs.API_DESCRIPTION)
                .license(new License()
                    .name(SwaggerDocs.LICENSE_NAME)
                    .url(SwaggerDocs.LICENSE_URL)))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description(SwaggerDocs.SECURITY_SCHEME_DESC)))
            .addServersItem(new Server()
                .url(SwaggerDocs.SERVER_URL)
                .description(SwaggerDocs.SERVER_DESC))
            .externalDocs(new ExternalDocumentation()
                .description(SwaggerDocs.EXTERNAL_DOCS_DESC)
                .url(SwaggerDocs.EXTERNAL_DOCS_URL));
    }
}
