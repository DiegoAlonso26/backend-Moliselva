package pe.edu.upeu.moliselva.produccion.application.port.out;

import java.util.List;
import java.util.Optional;
import pe.edu.upeu.moliselva.produccion.domain.DatosInsumo;
import pe.edu.upeu.moliselva.produccion.domain.DatosLote;
import pe.edu.upeu.moliselva.produccion.domain.EstadoLote;
import pe.edu.upeu.moliselva.produccion.domain.Lote;
import pe.edu.upeu.moliselva.produccion.domain.LoteInsumo;

public interface LotePort {
    List<Lote> listar();
    Optional<Lote> buscar(long id);
    Optional<Lote> bloquear(long id);
    Lote crear(DatosLote datos, long usuarioId);
    Lote actualizar(long id, DatosLote datos);
    void eliminar(long id);
    List<LoteInsumo> listarInsumos(long id);
    Optional<LoteInsumo> buscarInsumo(long id, long insumoId);
    LoteInsumo agregarInsumo(long id, DatosInsumo datos);
    LoteInsumo actualizarInsumo(long id, long insumoId, DatosInsumo datos);
    void eliminarInsumo(long id, long insumoId);
    boolean tieneInsumos(long id);
    boolean faltanInsumosFormula(long id, long formulaId);
    Lote cambiarEstado(long id, EstadoLote estado, long usuarioId);
    void marcarTerminado(long id, long usuarioId);
}
