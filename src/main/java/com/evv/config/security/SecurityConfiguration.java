package com.evv.config.security;

import com.evv.http.handler.ControllerExceptionHandler;
import com.evv.http.handler.RestControllerExceptionHandler;
import com.evv.dto.CustomUserDetails;
import com.evv.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static com.evv.database.entity.Role.ADMIN;
import static com.evv.database.entity.Role.CLIENT;
import static jakarta.servlet.DispatcherType.FORWARD;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
//@EnableWebMvc
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final UserService userService;

    private final PurchaseAuthorizationManager purchaseAuthorizationManager;

    private final ControllerExceptionHandler controllerExceptionHandler;

    private final RestControllerExceptionHandler restControllerExceptionHandler;

    @Bean
    @Order(1)
    public SecurityFilterChain restFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**", "/swagger-ui/**", "/v3/api-docs/**")
                .csrf(CsrfConfigurer::disable)
                .authorizeHttpRequests(authRegistry -> authRegistry
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/purchases/{purchaseId:\\d+}")
                            .access(purchaseAuthorizationManager) // Кастомная реализация AuthorizationManager. Сделано в образовательных целях.
                        // Все прочие ендпоинты, в образовательных целях, защищаются посредством MethodSecurity
                        .anyRequest().authenticated())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler(restControllerExceptionHandler)
                        .authenticationEntryPoint(restControllerExceptionHandler));
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain thymeleafFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authRegistry -> authRegistry
                        .dispatcherTypeMatchers(FORWARD).permitAll()
                        .requestMatchers("/users/login", "/users/registration").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users/clients").permitAll()
                        .requestMatchers("/purchase/**", "/users/logout").hasAuthority(CLIENT.getAuthority())
                        .requestMatchers("/users/**").hasAuthority(ADMIN.getAuthority())
                        .anyRequest().denyAll())
                .logout(logout -> logout
                        .logoutUrl("/users/logout")
                        .logoutSuccessUrl("/users/login")
                        .deleteCookies("JSESSIONID"))
                .formLogin(fLConfigurer -> fLConfigurer
                        .loginPage("/users/login")
                        .successHandler(customAuthSuccessHandler())
                        .permitAll())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler(controllerExceptionHandler))
                .oauth2Login(config -> config
                        .loginPage("/users/login")
                        .loginProcessingUrl("/users/login/oauth2/code/*")
                        .defaultSuccessUrl("/purchase/products", true)
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService()))
                        .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService()))
                );
        return http.build();
    }

    /**
     * Бин для возможности указания различных URL после успешной аутентификации
     * в зависимости от роли пользователя: CLIENT или ADMIN.
     * <p>Применяется после авторизации через форму Thymeleaf.
     * <p>Материал взят по ссылке:
     * <a href="https://www.baeldung.com/spring-redirect-after-login">Redirect to Different Pages After Login</a>
     */
    @Bean
    public AuthenticationSuccessHandler customAuthSuccessHandler() {
        return new CustomAuthSuccessHandler();
    }

    /**
     * Сервис для обработки аутентификации через Google посредством OpenID
     */
    private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        return userRequest -> {
            String email = userRequest.getIdToken().getClaim("email"); // Значит заходит через Google

            // Дату рождения приходится ставить самим, т.к. Google ее не передает:
            userService.createIfNotExistForOAuth(email, LocalDate.of(1970, 1, 1));

            UserDetails userDetails = userService.loadUserByUsername(email);
            DefaultOidcUser oidcUser = new DefaultOidcUser(userDetails.getAuthorities(), userRequest.getIdToken());

            Set<Method> userDetailsMethods = Set.of(CustomUserDetails.class.getMethods());
            return (OidcUser) Proxy.newProxyInstance(SecurityConfiguration.class.getClassLoader(),
                    new Class[]{CustomUserDetails.class, OidcUser.class},
                    (proxy, method, args) -> userDetailsMethods.contains(method)
                            ? method.invoke(userDetails, args)
                            : method.invoke(oidcUser, args));
        };
    }

    /**
     * Сервис для обработки аутентификации через Yandex посредством OAuth2
     */
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        return userRequest -> {
            OAuth2User user = new DefaultOAuth2UserService().loadUser(userRequest);

            String email = user.getName();
            String birthDateString = user.getAttribute("birthday");
            assert birthDateString != null;
            LocalDate birthDate = LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(birthDateString));

            userService.createIfNotExistForOAuth(email, birthDate);

            UserDetails userDetails = userService.loadUserByUsername(email);

            Set<Method> userDetailsMethods = Set.of(CustomUserDetails.class.getMethods());
            return (OAuth2User) Proxy.newProxyInstance(SecurityConfiguration.class.getClassLoader(),
                    new Class[]{CustomUserDetails.class, OAuth2User.class},
                    (proxy, method, args) -> userDetailsMethods.contains(method)
                            ? method.invoke(userDetails, args)
                            : method.invoke(user, args));
        };
    }
}
