package pe.edu.upeu.moliselva.application.formulas.gateway;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import pe.edu.upeu.moliselva.domain.formulas.Formula;
import pe.edu.upeu.moliselva.domain.formulas.FormulaDatos;
import pe.edu.upeu.moliselva.domain.formulas.FormulaDetalle;

public interface FormulaGateway {
    List<Formula> listar();
    Optional<Formula> buscar(long id);
    Formula crear(FormulaDatos datos, long idCreador);
    Formula actualizar(long id, FormulaDatos datos);
    void eliminar(long id);
    Formula cambiarVigencia(long id, boolean vigente);
    boolean usadaPorLote(long id);
    List<FormulaDetalle> listarDetalles(long id);
    Optional<FormulaDetalle> buscarDetalle(long id, long idInsumo);
    FormulaDetalle agregarDetalle(long id, long idInsumo, BigDecimal proporcion);
    FormulaDetalle actualizarDetalle(long id, long idInsumo, BigDecimal proporcion);
    void eliminarDetalle(long id, long idInsumo);
    boolean productoActivo(long idProducto);
    boolean insumoActivo(long idInsumo);
}
