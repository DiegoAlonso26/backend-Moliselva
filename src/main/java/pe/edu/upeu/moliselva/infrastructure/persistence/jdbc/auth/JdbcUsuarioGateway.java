package pe.edu.upeu.moliselva.infrastructure.persistence.jdbc.auth;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.application.auth.gateway.UsuarioGateway;
import pe.edu.upeu.moliselva.domain.auth.CuentaUsuario;
import pe.edu.upeu.moliselva.domain.auth.Usuario;
import pe.edu.upeu.moliselva.domain.auth.UsuarioDatos;

@Repository
public class JdbcUsuarioGateway implements UsuarioGateway {
    private static final String CUENTA = "SELECT u.id_usuario,u.nombres,u.usuario,u.password_hash,u.activo,r.nombre AS rol " +
        "FROM usuario u JOIN rol r ON r.id_rol=u.id_rol ";
    private static final RowMapper<CuentaUsuario> MAPPER = (rs,row) -> new CuentaUsuario(rs.getLong("id_usuario"),
        rs.getString("nombres"),rs.getString("usuario"),rs.getString("password_hash"),rs.getString("rol"),
        rs.getBoolean("activo"));
    private final JdbcTemplate db;

    public JdbcUsuarioGateway(JdbcTemplate db) { this.db = db; }

    @Override
    public Optional<CuentaUsuario> buscarPorUsuario(String usuario) {
        return db.query(CUENTA+"WHERE u.usuario=?",MAPPER,usuario).stream().findFirst();
    }

    @Override
    public Optional<CuentaUsuario> buscarPorId(long id) {
        return db.query(CUENTA+"WHERE u.id_usuario=?",MAPPER,id).stream().findFirst();
    }

    @Override
    public List<Usuario> listar() {
        return db.query("SELECT u.id_usuario,u.nombres,u.usuario,u.activo,r.nombre AS rol FROM usuario u " +
            "JOIN rol r ON r.id_rol=u.id_rol ORDER BY u.id_usuario",
            (rs,row) -> new Usuario(rs.getLong("id_usuario"),rs.getString("nombres"),
                rs.getString("usuario"),rs.getString("rol"),rs.getBoolean("activo")));
    }

    @Override
    public boolean existeRol(String rol) {
        Integer count = db.queryForObject("SELECT count(*) FROM rol WHERE nombre=?", Integer.class, rol);
        return count != null && count == 1;
    }

    @Override
    public long crear(UsuarioDatos datos, String hash) {
        Long id = db.queryForObject("INSERT INTO usuario(id_rol,nombres,usuario,password_hash) " +
            "SELECT id_rol,?,?,? FROM rol WHERE nombre=? RETURNING id_usuario", Long.class,
            datos.nombres(),datos.usuario(),hash,datos.rol());
        return id;
    }

    @Override
    public boolean actualizar(long id, UsuarioDatos datos, String hash) {
        return db.update("UPDATE usuario SET id_rol=(SELECT id_rol FROM rol WHERE nombre=?)," +
            "nombres=?,usuario=?,password_hash=COALESCE(?,password_hash) " +
            "WHERE id_usuario=? AND activo=true",
            datos.rol(),datos.nombres(),datos.usuario(),hash,id) == 1;
    }

    @Override
    public boolean desactivar(long id) {
        return db.update("UPDATE usuario SET activo=false WHERE id_usuario=? AND activo=true",id) == 1;
    }

    @Override
    public int contarClavesDemoPendientes() {
        Integer count = db.queryForObject("SELECT count(*) FROM usuario WHERE password_hash='HASH_DEMO_REEMPLAZAR'", Integer.class);
        return count == null ? 0 : count;
    }

    @Override
    public void asignarClaveDemo(String hash) {
        db.update("UPDATE usuario SET password_hash=? WHERE password_hash='HASH_DEMO_REEMPLAZAR' " +
            "AND usuario IN ('admin_demo','jefe_demo','operario_demo','contadora_demo')", hash);
    }
}
