package az.manga.demo.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final String BEARER_AUTH = "Bearer Authentication";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(buildInfo())
                .servers(List.of(
                        new Server().url(baseUrl).description("Current server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, buildSecurityScheme()));
    }

    private Info buildInfo() {
        return new Info()
                .title("az.manga API")
                .description("""
                        REST API для платформы az.manga.
                        
                        **Аутентификация:** большинство эндпоинтов требуют JWT Bearer токен.
                        Получите `accessToken` через `/authentication/login` или `/authentication/register`
                        и передавайте его в заголовке `Authorization: Bearer <token>`.
                        
                        **Роли:**
                        - `ADMIN` — полный доступ, включая создание/удаление манги, жанров, тегов
                        - `MODERATOR` — управление страницами и главами
                        - `USER` — чтение, комментарии, избранное, история, оценки
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("az.manga team")
                        .email("support@az.manga"));
    }

    private SecurityScheme buildSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Введите JWT accessToken, полученный при логине");
    }
}
