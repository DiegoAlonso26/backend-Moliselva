package pe.edu.upeu.moliselva.infrastructure.persistence.jdbc.produccion;

import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.produccion.gateway.BitacoraProduccionGateway;

@Repository
public class JdbcBitacoraProduccionGateway implements BitacoraProduccionGateway {
    private final BitacoraGateway bitacora;
    public JdbcBitacoraProduccionGateway(BitacoraGateway bitacora) { this.bitacora=bitacora; }
    @Override public void registrar(long usuarioId, String accion) {
        bitacora.registrar(usuarioId,accion,null);
    }
}
