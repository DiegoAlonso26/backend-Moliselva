package pe.edu.upeu.moliselva.infrastructure.persistence.jdbc.ventas;

import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.comun.BitacoraService;
import pe.edu.upeu.moliselva.application.ventas.gateway.BitacoraVentaGateway;

@Repository
public class JdbcBitacoraVentaGateway implements BitacoraVentaGateway {
    private final BitacoraService bitacora;
    public JdbcBitacoraVentaGateway(BitacoraService bitacora) { this.bitacora=bitacora; }
    @Override public void registrar(long usuarioId, String accion) {
        bitacora.registrar(usuarioId,accion,null);
    }
}
