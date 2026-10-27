package pe.edu.upeu.moliselva.produccion.application.port.in;

import java.util.List;
import pe.edu.upeu.moliselva.produccion.domain.Lote;
import pe.edu.upeu.moliselva.produccion.domain.LoteInsumo;

public interface ConsultarLotesUseCase {
    List<Lote> listar();
    Lote obtener(long id);
    List<LoteInsumo> listarInsumos(long id);
    LoteInsumo obtenerInsumo(long id, long insumoId);
}
