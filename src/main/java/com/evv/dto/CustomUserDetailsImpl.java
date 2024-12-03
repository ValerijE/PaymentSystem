package com.evv.dto;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.*;

@Data
public class CustomUserDetailsImpl implements CustomUserDetails {

    private final String username;

    private String password;

    private final boolean enabled;

    private final boolean accountNonExpired;

    private final boolean credentialsNonExpired;

    private final boolean accountNonLocked;

    private final Set<GrantedAuthority> authorities;

    /**
     * Одно из полей ради которого создавалась эта кастомная реализация UserDetails.
     * Нужно для получения данных авторизованного в данный момент пользователя.
     */
    private ClientReadDto client;
    /**
     * Одно из полей ради которого создавалась эта кастомная реализация UserDetails.
     * Нужно для получения данных авторизованного в данный момент администратора.
     */
    private AdminReadDto admin;

    // Нижеследующий код скопирован из org.springframework.security.core.userdetails.User,
    // кроме инициализации полей client и admin в конструкторе.

    public CustomUserDetailsImpl(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Set<GrantedAuthority> authorities, UserReadDto user) {
        Assert.isTrue(username != null && !"".equals(username) && password != null,
                "Cannot pass null or empty values to constructor");
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.credentialsNonExpired = credentialsNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.authorities = Collections.unmodifiableSet(sortAuthorities(authorities));

        if (user instanceof ClientReadDto cl) {
            this.client = cl;
            this.admin = null;
        } else if (user instanceof AdminReadDto ad) {
            this.admin = ad;
            this.client = null;
        } else {
            throw new UnknownFormatConversionException(
                    "Unknown instance of type User for id = %d".formatted(user.getId()));
        }
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }

    private static SortedSet<GrantedAuthority> sortAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Assert.notNull(authorities, "Cannot pass a null GrantedAuthority collection");
        // Ensure array iteration order is predictable (as per
        // UserDetails.getAuthorities() contract and SEC-717)
        SortedSet<GrantedAuthority> sortedAuthorities = new TreeSet<>(new AuthorityComparator());
        for (GrantedAuthority grantedAuthority : authorities) {
            Assert.notNull(grantedAuthority, "GrantedAuthority list cannot contain any null elements");
            sortedAuthorities.add(grantedAuthority);
        }
        return sortedAuthorities;
    }

    private static class AuthorityComparator implements Comparator<GrantedAuthority>, Serializable {

        @Override
        public int compare(GrantedAuthority g1, GrantedAuthority g2) {
            // Neither should ever be null as each entry is checked before adding it to
            // the set. If the authority is null, it is a custom authority and should
            // precede others.
            if (g2.getAuthority() == null) {
                return -1;
            }
            if (g1.getAuthority() == null) {
                return 1;
            }
            return g1.getAuthority().compareTo(g2.getAuthority());
        }

    }
}
