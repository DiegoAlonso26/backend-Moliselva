package pe.edu.upeu.moliselva.infrastructure.persistence.jdbc.bitacora;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.application.bitacora.gateway.BitacoraConsultaGateway;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;

@Repository
public class JdbcBitacoraGateway implements BitacoraGateway, BitacoraConsultaGateway {
    private static final Logger log = LoggerFactory.getLogger(JdbcBitacoraGateway.class);
    private static final String EVENTO = "SELECT b.id_bitacora,b.fecha_hora,b.accion,b.motivo,u.usuario " +
        "FROM bitacora b JOIN usuario u ON u.id_usuario=b.id_usuario ";
    private final JdbcTemplate db;

    public JdbcBitacoraGateway(JdbcTemplate db) { this.db=db; }

    @Override
    public void registrar(long usuarioId, String accion, String motivo) {
        db.update("INSERT INTO bitacora(id_usuario,accion,motivo) VALUES (?,?,?)",usuarioId,accion,motivo);
        log.info("bitacora usuarioId={} accion={}",usuarioId,accion);
    }

    @Override
    public List<Map<String,Object>> recientes() {
        return db.queryForList(EVENTO+"ORDER BY b.id_bitacora DESC LIMIT 200");
    }

    @Override
    public Optional<Map<String,Object>> buscar(long id) {
        return db.queryForList(EVENTO+"WHERE b.id_bitacora=?",id).stream().findFirst();
    }
}
