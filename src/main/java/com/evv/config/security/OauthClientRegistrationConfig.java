package com.evv.config.security;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

/**
 * Если смотрите этот код слитый с GitHub, то у Вас отсутствует файл secrets.properties и, следовательно,
 * поля с аннотацией @Value будут брать дефолтные значения, а соответствующий функционал не будет работать должным образом.
 */
@Configuration
@PropertySource(value = "classpath:secrets.properties")
@Setter
public class OauthClientRegistrationConfig {

    @Value("${spring.security.oauth2.client.registration.yandex.clientId:dummyYandexClientId}")
    private String yandexClientId;

    @Value("${spring.security.oauth2.client.registration.yandex.clientSecret:dummyYandexClientId}")
    private String yandexClientSecret;

    @Value("${spring.security.oauth2.client.registration.google.clientId:dummyGoogleClientId}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.clientSecret:dummyGoogleClientSecret}")
    private String googleClientSecret;

    /**
     * Бин для регистрации клиентов OAuth2 и OIDC аутентификации
     * <a href="https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html">...</a>
     */
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(yandexClientRegistration(), googleClientRegistration());
    }

    /**
     * Метод для настройки клиента Yandex ID
     * <a href="https://it-stories.ru/blog/web-dev/avtorizacija-na-sajte-cherez-jandeks/">...</a>
     */
    private ClientRegistration yandexClientRegistration() {
        return ClientRegistration.withRegistrationId("yandex")
                .clientId(yandexClientId)
                .clientSecret(yandexClientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/users/login/oauth2/code/{registrationId}")
                .authorizationUri("https://oauth.yandex.ru/authorize")
                .tokenUri("https://oauth.yandex.ru/token")
                .userInfoUri("https://login.yandex.ru/info")
                .userNameAttributeName("default_email")
                .clientName("yandex")
                .build();
    }

    private ClientRegistration googleClientRegistration() {
        return CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId(googleClientId)
                .clientSecret(googleClientSecret)
                .redirectUri("{baseUrl}/users/login/oauth2/code/{registrationId}")
                .build();
    }
}
