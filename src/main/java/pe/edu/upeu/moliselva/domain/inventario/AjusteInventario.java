package pe.edu.upeu.moliselva.domain.inventario;

import java.math.BigDecimal;

/** Ajuste compensatorio (AJUSTE_ENTRADA o AJUSTE_SALIDA) de un insumo o de un producto. */
public record AjusteInventario(String tipoMovimiento, Long idInsumo, Long idProducto,
        BigDecimal cantidad, String motivo) {

    /** Debe indicar exactamente un insumo o un producto, con id positivo. */
    public boolean itemValido() {
        return (idInsumo == null) != (idProducto == null)
            && (idInsumo == null || idInsumo > 0)
            && (idProducto == null || idProducto > 0);
    }
}
