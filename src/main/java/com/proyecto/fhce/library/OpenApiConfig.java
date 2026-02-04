package com.proyecto.fhce.library;

import java.util.Arrays;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Gestión Bibliográfica - API")
                        .version("1.0.0")
                        .description(
                                "API REST para el Sistema Centralizado de Gestión Bibliográfica de la Facultad de Humanidades y Ciencias de la Educación")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo")
                                .email("soporte@biblioteca.edu.bo")
                                .url("https://biblioteca.edu.bo"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentación completa del proyecto")
                        .url("https://docs.biblioteca.edu.bo"))
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:8098")
                                .description("Servidor de Desarrollo"),
                        new Server()
                                .url("https://api.biblioteca.edu.bo")
                                .description("Servidor de Producción")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                                .description("Ingrese el token JWT. Formato: Bearer {token}")));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .displayName("APIs Públicas")
                .pathsToMatch("/api/auth/**", "/api/libros/**", "/api/autores/**",
                        "/api/categorias/**", "/api/certificados/validar/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("usuarios")
                .displayName("Gestión de Usuarios")
                .pathsToMatch("/api/users/**", "/api/personas/**", "/api/roles/**", "/api/permisos/**",
                        "/api/carreras/**")
                .build();
    }

    @Bean
    public GroupedOpenApi bibliotecaApi() {
        return GroupedOpenApi.builder()
                .group("biblioteca")
                .displayName("Gestión Bibliográfica")
                .pathsToMatch("/api/libros/**", "/api/ejemplares/**", "/api/autores/**",
                        "/api/categorias/**", "/api/bibliotecas/**")
                .build();
    }

    @Bean
    public GroupedOpenApi prestamosApi() {
        return GroupedOpenApi.builder()
                .group("prestamos")
                .displayName("Préstamos y Reservas")
                .pathsToMatch("/api/prestamos/**", "/api/reservas/**", "/api/sanciones/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .displayName("Administración")
                .pathsToMatch("/api/dashboard/**", "/api/reportes/**", "/api/auditoria/**",
                        "/api/configuraciones-prestamo/**")
                .build();
    }
}
