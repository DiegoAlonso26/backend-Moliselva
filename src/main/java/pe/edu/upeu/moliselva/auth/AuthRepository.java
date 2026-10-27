package pe.edu.upeu.moliselva.auth;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuthRepository {
    private final JdbcTemplate db;
    public AuthRepository(JdbcTemplate db) { this.db = db; }

    public record UsuarioRow(long id, String nombres, String usuario, String hash, String rol, boolean activo) {
        public AuthDto.Usuario publicDto() { return new AuthDto.Usuario(id,nombres,usuario,rol); }
    }

    public Optional<UsuarioRow> findByName(String username) {
        return db.query("SELECT u.id_usuario,u.nombres,u.usuario,u.password_hash,u.activo,r.nombre AS rol " +
                "FROM usuario u JOIN rol r ON r.id_rol=u.id_rol WHERE u.usuario=?",
            (rs,row) -> new UsuarioRow(rs.getLong("id_usuario"),rs.getString("nombres"),
                rs.getString("usuario"),rs.getString("password_hash"),rs.getString("rol"),rs.getBoolean("activo")),
            username).stream().findFirst();
    }

    public Optional<UsuarioRow> findById(long id) {
        return db.query("SELECT u.id_usuario,u.nombres,u.usuario,u.password_hash,u.activo,r.nombre AS rol " +
                "FROM usuario u JOIN rol r ON r.id_rol=u.id_rol WHERE u.id_usuario=?",
            (rs,row) -> new UsuarioRow(rs.getLong("id_usuario"),rs.getString("nombres"),
                rs.getString("usuario"),rs.getString("password_hash"),rs.getString("rol"),rs.getBoolean("activo")),
            id).stream().findFirst();
    }

    public int countPlaceholderHashes() {
        Integer count = db.queryForObject("SELECT count(*) FROM usuario WHERE password_hash='HASH_DEMO_REEMPLAZAR'", Integer.class);
        return count == null ? 0 : count;
    }

    public void replacePlaceholderHashes(String hash) {
        db.update("UPDATE usuario SET password_hash=? WHERE password_hash='HASH_DEMO_REEMPLAZAR' " +
            "AND usuario IN ('admin_demo','jefe_demo','operario_demo','contadora_demo')", hash);
    }

    public boolean roleExists(String role) {
        Integer count = db.queryForObject("SELECT count(*) FROM rol WHERE nombre=?", Integer.class, role);
        return count != null && count == 1;
    }

    public long create(AuthDto.NuevoUsuario user, String hash) {
        Long id = db.queryForObject("INSERT INTO usuario(id_rol,nombres,usuario,password_hash) " +
            "SELECT id_rol,?,?,? FROM rol WHERE nombre=? RETURNING id_usuario", Long.class,
            user.nombres().trim(),user.usuario().trim(),hash,user.rol());
        return id;
    }

    public List<AuthDto.Usuario> list() {
        return db.query("SELECT u.id_usuario,u.nombres,u.usuario,r.nombre AS rol FROM usuario u " +
            "JOIN rol r ON r.id_rol=u.id_rol ORDER BY u.id_usuario",
            (rs,row) -> new AuthDto.Usuario(rs.getLong("id_usuario"),rs.getString("nombres"),
                rs.getString("usuario"),rs.getString("rol")));
    }
}
