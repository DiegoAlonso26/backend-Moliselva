package pe.edu.upeu.moliselva.comun;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BitacoraRepository {
    private final JdbcTemplate db;
    public BitacoraRepository(JdbcTemplate db) { this.db = db; }

    public void registrar(long usuarioId, String accion, String motivo) {
        db.update("INSERT INTO bitacora(id_usuario,accion,motivo) VALUES (?,?,?)",usuarioId,accion,motivo);
    }

    public List<Map<String,Object>> recientes() {
        return db.queryForList("SELECT b.id_bitacora,b.fecha_hora,b.accion,b.motivo,u.usuario " +
            "FROM bitacora b JOIN usuario u ON u.id_usuario=b.id_usuario ORDER BY b.id_bitacora DESC LIMIT 200");
    }
}
