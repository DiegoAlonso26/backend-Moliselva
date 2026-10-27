package pe.edu.upeu.moliselva.ventas.adapter.out.jdbc;

import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.comun.BitacoraService;
import pe.edu.upeu.moliselva.ventas.application.port.out.BitacoraVentaPort;

@Repository
public class JdbcBitacoraVentaAdapter implements BitacoraVentaPort {
    private final BitacoraService bitacora;
    public JdbcBitacoraVentaAdapter(BitacoraService bitacora) { this.bitacora=bitacora; }
    @Override public void registrar(long usuarioId, String accion) {
        bitacora.registrar(usuarioId,accion,null);
    }
}
