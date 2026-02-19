package com.aperdigon.ticketing_backend.api.shared.currentuser;

import java.util.UUID;

import com.aperdigon.ticketing_backend.application.shared.CurrentUser;
import com.aperdigon.ticketing_backend.domain.user.UserId;
import com.aperdigon.ticketing_backend.domain.user.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class JwtCurrentUserProvider implements CurrentUserProvider {

    @Override
    public CurrentUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            throw new IllegalStateException("No JWT authentication present");
        }

        // sub = UUID del usuario
        String sub = jwtAuth.getToken().getSubject();
        UUID userId = UUID.fromString(sub);

        // rol: lo obtenemos de authorities ROLE_*
        // (para MVP asumimos 1 rol principal)
        UserRole role = jwtAuth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .map(UserRole::valueOf)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("JWT has no ROLE_* authority"));

        return new CurrentUser(new UserId(userId), role);
    }
}
