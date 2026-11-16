package pe.edu.upeu.moliselva.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import pe.edu.upeu.moliselva.application.shared.Actor;
import pe.edu.upeu.moliselva.application.shared.ActorActual;

/**
 * Obtiene el actor del JWT ya validado por {@code comun.SecurityConfig}:
 * {@code uid} es el id, {@code sub} el usuario y la autoridad ROLE_* el rol.
 */
@Component
public class JwtActorActual implements ActorActual {

    @Override
    public Actor obtener() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("No hay un usuario autenticado con JWT");
        }
        String rol = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
            .filter(a -> a.startsWith("ROLE_")).findFirst()
            .map(a -> a.substring("ROLE_".length()))
            .orElseThrow(() -> new IllegalStateException("Rol autenticado ausente"));
        return new Actor(((Number)jwt.getClaim("uid")).longValue(),jwt.getSubject(),rol);
    }
}
