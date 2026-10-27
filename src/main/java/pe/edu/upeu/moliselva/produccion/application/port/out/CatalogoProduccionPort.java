package pe.edu.upeu.moliselva.produccion.application.port.out;

public interface CatalogoProduccionPort {
    boolean productoActivo(long idProducto);
    boolean formulaVigente(long idFormula, long idProducto);
    boolean formulaTieneDetalle(long idFormula);
    boolean insumoActivo(long idInsumo);
    boolean insumoEnFormula(long idLote, long idInsumo);
}
