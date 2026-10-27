package pe.edu.upeu.moliselva.recepcion.domain;

import java.math.BigDecimal;

public record RecepcionDatos(long idProveedor, BigDecimal pesoBrutoKg,
    BigDecimal taraKg, BigDecimal pesoSacosKg, BigDecimal humedadPct,
    BigDecimal mermaLimpiezaKg) {}
