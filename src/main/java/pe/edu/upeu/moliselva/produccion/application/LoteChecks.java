package pe.edu.upeu.moliselva.produccion.application;

import pe.edu.upeu.moliselva.produccion.application.port.out.CatalogoProduccionPort;
import pe.edu.upeu.moliselva.produccion.application.port.out.InventarioProduccionPort;
import pe.edu.upeu.moliselva.produccion.domain.DatosLote;
import pe.edu.upeu.moliselva.produccion.domain.Lote;
import pe.edu.upeu.moliselva.produccion.domain.LotePolicy;

final class LoteChecks {
    private LoteChecks() {}
    static ProduccionFailure missing(String type) {
        return new ProduccionFailure(404,type+"_NO_ENCONTRADO","El registro no existe");
    }
    static ProduccionFailure locked(String message) {
        return new ProduccionFailure(409,"REGISTRO_NO_EDITABLE",message);
    }
    static void require(boolean test, String code, String message) {
        if (!test) { throw new ProduccionFailure(400,code,message); }
    }
    static void datos(DatosLote datos, CatalogoProduccionPort catalogo) {
        require(LotePolicy.tamanoValido(datos.tamanoKg()),"TAMANO_INVALIDO",
            "El lote debe ser de 500 o 1000 kg");
        require(catalogo.productoActivo(datos.idProducto()),"REFERENCIA_INVALIDA",
            "El registro relacionado no existe o está inactivo");
        require(catalogo.formulaVigente(datos.idFormula(),datos.idProducto()),"FORMULA_INVALIDA",
            "La fórmula no corresponde al producto o no está vigente");
        require(catalogo.formulaTieneDetalle(datos.idFormula()),"FORMULA_SIN_DETALLE",
            "La fórmula debe tener al menos un ingrediente");
    }
    static void planificado(Lote lote, InventarioProduccionPort inventario) {
        if (!LotePolicy.editable(lote,inventario.tieneMovimientos(lote.idLote()))) {
            throw locked("Solo puede editar un lote planificado sin movimientos");
        }
    }
    static void insumoCorregible(Lote lote, InventarioProduccionPort inventario) {
        if (!LotePolicy.insumoCorregible(lote,inventario.tieneMovimientos(lote.idLote()))) {
            throw locked("Los insumos del lote ya no se pueden modificar");
        }
    }
    static void insumoActivo(CatalogoProduccionPort catalogo, long idInsumo) {
        require(catalogo.insumoActivo(idInsumo),"REFERENCIA_INVALIDA",
            "El registro relacionado no existe o está inactivo");
    }
}
