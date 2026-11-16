package pe.edu.upeu.moliselva.domain.inventario;

import java.math.BigDecimal;

/** Stock actual de un insumo o producto (vista vw_stock_actual). */
public record ItemStock(String tipoItem, long idItem, String nombre, String unidadMedida, BigDecimal stockActual) {
    public static final String INSUMO = "INSUMO";
    public static final String PRODUCTO = "PRODUCTO";

    public static boolean tipoValido(String tipo) {
        return INSUMO.equals(tipo) || PRODUCTO.equals(tipo);
    }
}
