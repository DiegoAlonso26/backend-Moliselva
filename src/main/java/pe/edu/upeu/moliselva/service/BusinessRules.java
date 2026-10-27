package pe.edu.upeu.moliselva.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class BusinessRules {
    private static final BigDecimal HUMEDAD_NORMAL = new BigDecimal("14.5");
    private static final BigDecimal HUMEDAD_AUTORIZABLE = new BigDecimal("15");
    private static final BigDecimal CIEN = new BigDecimal("100");

    private BusinessRules() {}

    public static BigDecimal pesoNeto(BigDecimal bruto, BigDecimal tara, BigDecimal sacos) {
        BigDecimal neto = bruto.subtract(tara).subtract(sacos).setScale(2, RoundingMode.HALF_UP);
        if (neto.signum() <= 0) throw new IllegalArgumentException("El peso neto debe ser positivo");
        return neto;
    }

    public static String estadoRecepcion(BigDecimal humedad) {
        if (humedad.signum() < 0 || humedad.compareTo(CIEN) > 0) {
            throw new IllegalArgumentException("La humedad debe estar entre 0 y 100");
        }
        if (humedad.compareTo(HUMEDAD_AUTORIZABLE) > 0) return "RECHAZADA";
        return humedad.compareTo(HUMEDAD_NORMAL) > 0 ? "PENDIENTE" : "ACEPTADA";
    }

    public static BigDecimal entradaKardex(BigDecimal neto, BigDecimal mermaLimpieza) {
        if (mermaLimpieza.signum() < 0 || mermaLimpieza.compareTo(neto) >= 0) {
            throw new IllegalArgumentException("La merma de limpieza debe ser menor que el peso neto");
        }
        return neto.subtract(mermaLimpieza).setScale(3, RoundingMode.HALF_UP);
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
