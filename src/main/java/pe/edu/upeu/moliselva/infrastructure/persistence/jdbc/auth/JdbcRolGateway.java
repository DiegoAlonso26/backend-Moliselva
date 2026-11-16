package pe.edu.upeu.moliselva.infrastructure.persistence.jdbc.auth;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.application.auth.gateway.RolGateway;
import pe.edu.upeu.moliselva.domain.auth.Rol;

@Repository
public class JdbcRolGateway implements RolGateway {
    private static final RowMapper<Rol> MAPPER = (rs,n) ->
        new Rol(rs.getShort("id_rol"),rs.getString("nombre"),rs.getString("descripcion"));
    private final JdbcTemplate db;

    public JdbcRolGateway(JdbcTemplate db) { this.db=db; }

    @Override
    public List<Rol> listar() {
        return db.query("SELECT id_rol,nombre,descripcion FROM rol ORDER BY id_rol",MAPPER);
    }

    @Override
    public Optional<Rol> buscar(short id) {
        return db.query("SELECT id_rol,nombre,descripcion FROM rol WHERE id_rol=?",MAPPER,id).stream().findFirst();
    }
}
