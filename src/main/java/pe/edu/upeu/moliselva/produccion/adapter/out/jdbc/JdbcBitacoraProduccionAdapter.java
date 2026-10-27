package pe.edu.upeu.moliselva.produccion.adapter.out.jdbc;

import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.comun.BitacoraService;
import pe.edu.upeu.moliselva.produccion.application.port.out.BitacoraProduccionPort;

@Repository
public class JdbcBitacoraProduccionAdapter implements BitacoraProduccionPort {
    private final BitacoraService bitacora;
    public JdbcBitacoraProduccionAdapter(BitacoraService bitacora) { this.bitacora=bitacora; }
    @Override public void registrar(long usuarioId, String accion) {
        bitacora.registrar(usuarioId,accion,null);
    }
}
