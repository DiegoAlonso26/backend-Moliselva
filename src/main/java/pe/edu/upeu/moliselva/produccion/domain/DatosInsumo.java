package pe.edu.upeu.moliselva.produccion.domain;

import java.math.BigDecimal;

public record DatosInsumo(long idInsumo, BigDecimal cantidadCalculadaKg,
    BigDecimal cantidadRealKg) {}
