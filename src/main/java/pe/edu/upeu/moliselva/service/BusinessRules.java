package pe.edu.upeu.moliselva.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import pe.edu.upeu.moliselva.recepcion.domain.RecepcionPolicy;

public final class BusinessRules {
    private static final BigDecimal CIEN = new BigDecimal("100");

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
        if (tamanoKg != 500 && tamanoKg != 1000) throw new IllegalArgumentException("El lote debe ser de 500 o 1000 kg");
        if (porcentaje.signum() <= 0 || porcentaje.compareTo(CIEN) > 0) {
            throw new IllegalArgumentException("Porcentaje de macro inválido");
        }
        return BigDecimal.valueOf(tamanoKg).multiply(porcentaje).divide(CIEN, 3, RoundingMode.HALF_UP);
    }

    public static BigDecimal kgDespacho(int sacos, BigDecimal pesoSacoKg) {
        if (sacos <= 0 || pesoSacoKg.signum() <= 0) throw new IllegalArgumentException("Sacos o peso por saco inválidos");
        return pesoSacoKg.multiply(BigDecimal.valueOf(sacos)).setScale(3, RoundingMode.HALF_UP);
    }
}
