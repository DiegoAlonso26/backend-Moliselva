package pe.edu.upeu.moliselva.infrastructure.persistence.jdbc.produccion;

import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.comun.BitacoraService;
import pe.edu.upeu.moliselva.application.produccion.gateway.BitacoraProduccionGateway;

@Repository
public class JdbcBitacoraProduccionGateway implements BitacoraProduccionGateway {
    private final BitacoraService bitacora;
    public JdbcBitacoraProduccionGateway(BitacoraService bitacora) { this.bitacora=bitacora; }
    @Override public void registrar(long usuarioId, String accion) {
        bitacora.registrar(usuarioId,accion,null);
    }
}
