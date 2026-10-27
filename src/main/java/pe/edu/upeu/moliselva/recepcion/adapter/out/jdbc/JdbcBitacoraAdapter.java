package pe.edu.upeu.moliselva.recepcion.adapter.out.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.comun.BitacoraRepository;
import pe.edu.upeu.moliselva.recepcion.application.port.out.BitacoraPort;

@Repository
public class JdbcBitacoraAdapter implements BitacoraPort {
    private static final Logger log=LoggerFactory.getLogger(JdbcBitacoraAdapter.class);
    private final BitacoraRepository bitacora;
    public JdbcBitacoraAdapter(BitacoraRepository bitacora) { this.bitacora=bitacora; }
    @Override
    public void registrar(long usuarioId, String accion, String motivo) {
        bitacora.registrar(usuarioId,accion,motivo);
        log.info("bitacora usuarioId={} accion={}",usuarioId,accion);
    }
}
