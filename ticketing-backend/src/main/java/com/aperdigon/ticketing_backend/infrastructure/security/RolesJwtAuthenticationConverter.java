package com.aperdigon.ticketing_backend.infrastructure.security;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

public final class RolesJwtAuthenticationConverter extends JwtAuthenticationConverter {

    public RolesJwtAuthenticationConverter() {
        setJwtGrantedAuthoritiesConverter(new RolesConverter());
    }

    static final class RolesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Object rolesClaim = jwt.getClaims().get("roles");
            if (rolesClaim == null) {
                return List.of();
            }

            // Esperamos: "roles": ["USER","AGENT"]
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) rolesClaim;

            return roles.stream()
                    .map(String::trim)
                    .filter(r -> !r.isEmpty())
                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toUnmodifiableList());
        }
    }
}
