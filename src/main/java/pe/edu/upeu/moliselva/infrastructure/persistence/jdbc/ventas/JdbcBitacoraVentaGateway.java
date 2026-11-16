package pe.edu.upeu.moliselva.infrastructure.persistence.jdbc.ventas;

import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.ventas.gateway.BitacoraVentaGateway;

@Repository
public class JdbcBitacoraVentaGateway implements BitacoraVentaGateway {
    private final BitacoraGateway bitacora;
    public JdbcBitacoraVentaGateway(BitacoraGateway bitacora) { this.bitacora=bitacora; }
    @Override public void registrar(long usuarioId, String accion) {
        bitacora.registrar(usuarioId,accion,null);
    }
}
