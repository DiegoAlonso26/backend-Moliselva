package pe.edu.upeu.moliselva.application.produccion.usecase;

import java.util.List;
import pe.edu.upeu.moliselva.domain.produccion.Lote;
import pe.edu.upeu.moliselva.domain.produccion.LoteInsumo;

public interface ConsultarLotesUseCase {
    List<Lote> listar();
    Lote obtener(long id);
    List<LoteInsumo> listarInsumos(long id);
    LoteInsumo obtenerInsumo(long id, long insumoId);
}
