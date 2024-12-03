package com.evv.config;

import com.evv.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.envers.repository.config.EnableEnversRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

@EnableJpaAuditing
@EnableEnversRepositories(basePackageClasses = ApplicationRunner.class)
@Configuration
public class AuditConfiguration {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            if (SecurityContextHolder.getContext().getAuthentication() == null) { // Значит скорее всего произошла аутентификации через OAuth2 или OIDC и далее пользователь будет сохранен в БД под авторством "maybeOAuth2"
                return Optional.of("maybeOAuth2"); // Только для сохранения нового пользователя в таблицу users. В других таблицах "maybeOAuth2" быть не должно.
            } else {
                Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (p instanceof String pStr) { // Проверка нужна для случая регистрации нового пользователя. Тогда вместо UserDetails в Principle лежит String "anonymousUser".
                    return Optional.of(pStr);
                } else {
                    return Optional.of(((UserDetails) p).getUsername());
                }
            }
        };
    }
}