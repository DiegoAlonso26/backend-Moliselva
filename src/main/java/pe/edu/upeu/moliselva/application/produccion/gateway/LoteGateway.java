package pe.edu.upeu.moliselva.application.produccion.gateway;

import java.util.List;
import java.util.Optional;
import pe.edu.upeu.moliselva.domain.produccion.DatosInsumo;
import pe.edu.upeu.moliselva.domain.produccion.DatosLote;
import pe.edu.upeu.moliselva.domain.produccion.EstadoLote;
import pe.edu.upeu.moliselva.domain.produccion.Lote;
import pe.edu.upeu.moliselva.domain.produccion.LoteInsumo;

public interface LoteGateway {
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
