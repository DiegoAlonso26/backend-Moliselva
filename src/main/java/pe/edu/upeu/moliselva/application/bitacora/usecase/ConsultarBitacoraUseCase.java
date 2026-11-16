package pe.edu.upeu.moliselva.application.bitacora.usecase;

import java.util.List;
import java.util.Map;

/** Últimos 200 eventos y detalle de un evento. ADMINISTRADOR o JEFE_PLANTA (se exige en la presentación). */
public interface ConsultarBitacoraUseCase {
    List<Map<String,Object>> recientes();
    Map<String,Object> obtener(long id);
}
