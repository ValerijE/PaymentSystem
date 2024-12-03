package com.evv.dto;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Интерфейс нужен для указания поля interfaces при создании Proxy#newProxyInstance(..) в методе
 * SecurityConfiguration#oidcUserService(..).
 */
public interface CustomUserDetails extends UserDetails, CredentialsContainer {

    public ClientReadDto getClient();

    public AdminReadDto getAdmin();
}
