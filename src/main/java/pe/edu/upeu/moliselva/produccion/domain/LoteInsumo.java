package pe.edu.upeu.moliselva.produccion.domain;

import java.math.BigDecimal;

public record LoteInsumo(long idLote, long idInsumo, BigDecimal cantidadCalculadaKg,
    BigDecimal cantidadRealKg) {
    public BigDecimal cantidadConsumidaKg() {
        return cantidadRealKg == null ? cantidadCalculadaKg : cantidadRealKg;
    }
}
