package pe.edu.upeu.moliselva.infrastructure.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;
import pe.edu.upeu.moliselva.application.auth.gateway.EmisorToken;
import pe.edu.upeu.moliselva.domain.auth.CuentaUsuario;

/** JWT HS256 con issuer, emisión, vencimiento, sub = usuario, uid y roles. */
@Component
public class JwtEmisorToken implements EmisorToken {
    private final JwtEncoder encoder;
    private final String issuer;
    private final long ttlMinutes;

    public JwtEmisorToken(JwtEncoder encoder, @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.ttl-minutes}") long ttlMinutes) {
        this.encoder=encoder; this.issuer=issuer; this.ttlMinutes=ttlMinutes;
    }

    @Override
    public String emitir(CuentaUsuario cuenta) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(issuer).issuedAt(now)
            .expiresAt(now.plus(ttlMinutes,ChronoUnit.MINUTES)).subject(cuenta.usuario())
            .claim("uid",cuenta.id()).claim("roles",List.of(cuenta.rol())).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
    }
}
