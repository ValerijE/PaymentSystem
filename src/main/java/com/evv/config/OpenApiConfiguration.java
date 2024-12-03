package com.evv.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

/**
 * Конфигурация для донастройки аутентификации springdoc-openapi через Google. <br>
 * Прочие настройки springdoc-openapi прописаны в application.yml с префиксом springdoc. <br>
 * Если смотрите этот код слитый с GitHub, то у Вас отсутствует файл secrets.properties и, следовательно,
 * поля с аннотацией @Value будут брать дефолтные значения, а соответствующий функционал не будет работать должным образом.
 */
@Configuration
@RequiredArgsConstructor
@SecurityScheme(
        name = "oauth2",
        type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(
                authorizationCode = @OAuthFlow(
                        authorizationUrl = "http://localhost:8080/oauth2/authorization/google",
                        tokenUrl = "https://www.googleapis.com/oauth2/v4/token"
                )
        )
)
@PropertySource(value = "classpath:secrets.properties")
@Setter
public class OpenApiConfiguration {

    private final AppProperties appProperties;

    @Value("${springdoc.swagger-ui.oauth.client-id:dummyClientId}")
    private String clientId;

    @Value("${springdoc.swagger-ui.oauth.clientSecret:dummyClientSecret}")
    private String clientSecret;

    @Value("${springdoc.swagger-ui.oauth.scopes:dummyScopes}")
    private List<String> scopes;

    /**
    * Бин переопределяет дефолтный для возможности указания свойств содержащих секретные данные. <br>
    * Секретные данные вынесены в файл secrets.properties, который не подлежит публикации.
     */
    @Bean
    @Primary
    public SwaggerUiOAuthProperties security() {
        SwaggerUiOAuthProperties swaggerUiOAuthProperties = new SwaggerUiOAuthProperties();
        swaggerUiOAuthProperties.setClientId(clientId);
        swaggerUiOAuthProperties.setClientSecret(clientSecret);
        swaggerUiOAuthProperties.setScopes(scopes);
        return swaggerUiOAuthProperties;
    }

    @Bean
    public GroupedOpenApi prodApi() {

        final Info theInfo = new Info()
                .title("Payment system")
                // language=html
                .description("""
                    <ins>Способы авторизации клиента Swagger UI:</ins>
                    <ol>
                        <li>
                            Протокол OpenID Connect с алгоритмом Authorization Code Flow с использованием аккаунта Google - кнопка Authorize ниже;
                        </li>
                        <br>
                        <li>
                            Ввод логина и пароля на html-странице <a href=http://localhost:8080/users/login>http://localhost:8080/users/login</a>,\s
                            открытой в том же интернет-браузере, в котором открыт Swagger UI, для чего предусмотрены учетные записи:
                            <ul>
                                <li>
                                    роль CLIENT: login - client1@gmail.com, password - 123,
                                </li>
                                <li>
                                    роль ADMIN: login - admin1@yandex.ru, password - 222.
                                </li>
                            </ul>
                            После успешной авторизации по вышеуказанной ссылке приступайте к работе в Swagger UI.
                        </li>
                    </ol>
                    """
                )
                .version(appProperties.getVersion())
                .contact(new Contact()
                        .name("ValerijE")
                        .email("vbkvkb@internet.ru")
                );

        return GroupedOpenApi.builder()
                .addOpenApiCustomizer(openApi -> {
                            openApi.setServers(List.of(new Server().url("http://localhost:8080")));
                            openApi.info(theInfo);
                        }
                )
                .group("Payment system API: version %s".formatted(appProperties.getVersion()))
                .build();
    }
}
