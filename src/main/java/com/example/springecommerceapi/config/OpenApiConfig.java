package com.example.springecommerceapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Spring E-Commerce API",
                version = "1.0.0",
                description = "E-Commerce uygulaması için REST API dokümantasyonu"
        )
)
public class OpenApiConfig {

}
