package pe.edu.upeu.moliselva.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import pe.edu.upeu.moliselva.recepcion.domain.RecepcionPolicy;
import pe.edu.upeu.moliselva.produccion.domain.LotePolicy;

public final class BusinessRules {
    private BusinessRules() {}

    public static BigDecimal pesoNeto(BigDecimal bruto, BigDecimal tara, BigDecimal sacos) {
        return RecepcionPolicy.pesoNeto(bruto,tara,sacos);
    }

    public static String estadoRecepcion(BigDecimal humedad) {
        return RecepcionPolicy.estadoRecepcion(humedad).name();
    }

    public static BigDecimal entradaKardex(BigDecimal neto, BigDecimal mermaLimpieza) {
        return RecepcionPolicy.entradaKardex(neto,mermaLimpieza);
    }

    public static BigDecimal kgMacro(int tamanoKg, BigDecimal porcentaje) {
        return LotePolicy.kgMacro(tamanoKg,porcentaje);
    }

    public static BigDecimal kgDespacho(int sacos, BigDecimal pesoSacoKg) {
        if (sacos <= 0 || pesoSacoKg.signum() <= 0) throw new IllegalArgumentException("Sacos o peso por saco inválidos");
        return pesoSacoKg.multiply(BigDecimal.valueOf(sacos)).setScale(3, RoundingMode.HALF_UP);
    }
}
