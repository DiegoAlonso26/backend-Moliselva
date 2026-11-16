package pe.edu.upeu.moliselva.application.bitacora.usecase;

import java.util.List;
import java.util.Map;
import pe.edu.upeu.moliselva.application.bitacora.gateway.BitacoraConsultaGateway;
import pe.edu.upeu.moliselva.application.shared.FallaAplicacion;

public class ConsultarBitacora implements ConsultarBitacoraUseCase {
    private final BitacoraConsultaGateway bitacora;

    public ConsultarBitacora(BitacoraConsultaGateway bitacora) { this.bitacora = bitacora; }

    @Override
    public List<Map<String,Object>> recientes() { return bitacora.recientes(); }

    @Override
    public Map<String,Object> obtener(long id) {
        return bitacora.buscar(id).orElseThrow(() ->
            FallaAplicacion.noEncontrado("EVENTO_NO_ENCONTRADO","El evento no existe"));
    }
}
