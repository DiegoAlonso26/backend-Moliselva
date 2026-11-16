package pe.edu.upeu.moliselva.application.bitacora.gateway;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lectura de la bitácora. Cada evento se entrega como fila (columna → valor) para
 * conservar exactamente el JSON publicado: id_bitacora, fecha_hora, accion, motivo y usuario.
 */
public interface BitacoraConsultaGateway {
    List<Map<String,Object>> recientes();
    Optional<Map<String,Object>> buscar(long id);
}
