package pe.edu.upeu.moliselva.application.produccion.gateway;

public interface CatalogoProduccionGateway {
    boolean productoActivo(long idProducto);
    boolean formulaVigente(long idFormula, long idProducto);
    boolean formulaTieneDetalle(long idFormula);
    boolean insumoActivo(long idInsumo);
    boolean insumoEnFormula(long idLote, long idInsumo);
}
