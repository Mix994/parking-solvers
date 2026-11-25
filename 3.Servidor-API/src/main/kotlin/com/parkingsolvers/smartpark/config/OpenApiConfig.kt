package com.parkingsolvers.smartpark.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        val schemeName = "bearerAuth"
        return OpenAPI()
            .info(
                Info()
                    .title("SmartPark API")
                    .version("0.0.1")
                    .description("API REST para controle de estacionamento do projeto Parking Solvers (SmartPark).")
            )
            .addSecurityItem(SecurityRequirement().addList(schemeName))
            .components(
                Components().addSecuritySchemes(
                    schemeName,
                    SecurityScheme()
                        .name(schemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            )
    }
}
