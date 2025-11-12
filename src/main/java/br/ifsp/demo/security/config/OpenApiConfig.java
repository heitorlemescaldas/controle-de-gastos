package br.ifsp.demo.security.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  // 1) Define o esquema Bearer JWT (aparece o botão "Authorize" no Swagger)
  @Bean
  public OpenAPI apiInfo() {
    return new OpenAPI()
        .info(new Info()
            .title("Controle de Gastos API")
            .version("v1"))
        .components(new Components().addSecuritySchemes("bearerAuth",
            new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")));
  }

  // 2) Aplica o requisito de segurança (bearerAuth) globalmente,
  //    EXCETO nas rotas públicas de registro e login.
  @Bean
  public OpenApiCustomizer globalSecurityCustomizer() {
    return openApi -> openApi.getPaths().forEach((path, pathItem) -> {
      boolean isPublic = path.equals("/api/v1/register") || path.equals("/api/v1/authenticate");
      if (!isPublic && pathItem != null && pathItem.readOperations() != null) {
        pathItem.readOperations().forEach(op ->
            op.addSecurityItem(new SecurityRequirement().addList("bearerAuth")));
      }
    });
  }
}
