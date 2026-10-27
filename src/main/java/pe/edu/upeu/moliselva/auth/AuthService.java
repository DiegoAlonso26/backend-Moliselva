package pe.edu.upeu.moliselva.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.moliselva.comun.ApiException;
import pe.edu.upeu.moliselva.comun.BitacoraService;

@Service
public class AuthService {
    private final AuthRepository users;
    private final PasswordEncoder passwords;
    private final JwtEncoder encoder;
    private final BitacoraService audit;
    private final String issuer;
    private final long ttlMinutes;

    public AuthService(AuthRepository users, PasswordEncoder passwords, JwtEncoder encoder,
            BitacoraService audit, @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.ttl-minutes}") long ttlMinutes) {
        this.users=users; this.passwords=passwords; this.encoder=encoder; this.audit=audit;
        this.issuer=issuer; this.ttlMinutes=ttlMinutes;
    }

    public AuthDto.LoginResponse login(AuthDto.LoginRequest input) {
        var user = users.findByName(input.usuario()).orElse(null);
        if (user == null || !user.activo() || !passwords.matches(input.password(),user.hash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED,"CREDENCIALES_INVALIDAS","Usuario o contraseña incorrectos");
        }
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(issuer).issuedAt(now)
            .expiresAt(now.plus(ttlMinutes,ChronoUnit.MINUTES)).subject(user.usuario())
            .claim("uid",user.id()).claim("roles",List.of(user.rol())).build();
        String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
        audit.registrar(user.id(),"LOGIN usuario="+user.usuario(),null);
        return new AuthDto.LoginResponse(token,user.publicDto());
    }

    public AuthDto.Usuario current(Jwt jwt) {
        long id = ((Number)jwt.getClaim("uid")).longValue();
        return users.findById(id).filter(AuthRepository.UsuarioRow::activo)
            .map(AuthRepository.UsuarioRow::publicDto)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,"NO_AUTENTICADO","Usuario inactivo o inexistente"));
    }

    @Transactional
    public AuthDto.Usuario create(AuthDto.NuevoUsuario input, long adminId) {
        if (!users.roleExists(input.rol())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,"ROL_INVALIDO","Rol no reconocido");
        }
        long id = users.create(input,passwords.encode(input.password()));
        audit.registrar(adminId,"CREAR_USUARIO id="+id,null);
        return new AuthDto.Usuario(id,input.nombres().trim(),input.usuario().trim(),input.rol(),true);
    }

    public List<AuthDto.Usuario> list() { return users.list(); }

    public AuthDto.Usuario find(long id) {
        return users.findById(id).map(AuthRepository.UsuarioRow::publicDto)
            .orElseThrow(() -> missing());
    }

    @Transactional
    public AuthDto.Usuario update(long id, AuthDto.ActualizarUsuario input, long adminId) {
        if (!users.roleExists(input.rol())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,"ROL_INVALIDO","Rol no reconocido");
        }
        if (id == adminId && !"ADMINISTRADOR".equals(input.rol())) {
            throw new ApiException(HttpStatus.CONFLICT,"ULTIMO_ADMINISTRADOR","No puede quitarse su propio rol de administrador");
        }
        String hash = input.password() == null ? null : passwords.encode(input.password());
        if (!users.update(id,input,hash)) { throw missing(); }
        audit.registrar(adminId,"ACTUALIZAR_USUARIO id="+id,null);
        return find(id);
    }

    @Transactional
    public void delete(long id, long adminId) {
        if (id == adminId) {
            throw new ApiException(HttpStatus.CONFLICT,"ULTIMO_ADMINISTRADOR","No puede desactivar su propia cuenta");
        }
        if (!users.deactivate(id)) { throw missing(); }
        audit.registrar(adminId,"DESACTIVAR_USUARIO id="+id,null);
    }

    private ApiException missing() {
        return new ApiException(HttpStatus.NOT_FOUND,"USUARIO_NO_ENCONTRADO","El usuario no existe o está inactivo");
    }
}
