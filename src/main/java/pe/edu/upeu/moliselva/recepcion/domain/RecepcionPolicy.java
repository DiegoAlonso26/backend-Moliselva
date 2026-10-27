package pe.edu.upeu.moliselva.recepcion.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class RecepcionPolicy {
    private static final BigDecimal HUMEDAD_NORMAL = new BigDecimal("14.5");
    private static final BigDecimal HUMEDAD_AUTORIZABLE = new BigDecimal("15");
    private static final BigDecimal CIEN = new BigDecimal("100");

    private RecepcionPolicy() {}

    public record Evaluacion(BigDecimal pesoNetoKg, BigDecimal entradaKardexKg,
        EstadoRecepcion estado) {}

    public static Evaluacion evaluar(RecepcionDatos datos) {
        BigDecimal neto = pesoNeto(datos.pesoBrutoKg(),datos.taraKg(),datos.pesoSacosKg());
        BigDecimal entrada = entradaKardex(neto,datos.mermaLimpiezaKg());
        EstadoRecepcion estado = estadoRecepcion(datos.humedadPct());
        return new Evaluacion(neto,entrada,estado);
    }

    public static BigDecimal pesoNeto(BigDecimal bruto, BigDecimal tara, BigDecimal sacos) {
        if (bruto == null || tara == null || sacos == null || bruto.signum() <= 0 ||
                tara.signum() < 0 || sacos.signum() < 0) {
            throw new IllegalArgumentException("Los pesos deben ser válidos y no negativos");
        }
        BigDecimal neto = bruto.subtract(tara).subtract(sacos).setScale(2,RoundingMode.HALF_UP);
        if (neto.signum() <= 0) { throw new IllegalArgumentException("El peso neto debe ser positivo"); }
        return neto;
    }

    public static BigDecimal entradaKardex(BigDecimal neto, BigDecimal merma) {
        if (neto == null || merma == null || merma.signum() < 0 || merma.compareTo(neto) >= 0) {
            throw new IllegalArgumentException("La merma de limpieza debe ser menor que el peso neto");
        }
        return neto.subtract(merma).setScale(3,RoundingMode.HALF_UP);
    }

    public static EstadoRecepcion estadoRecepcion(BigDecimal humedad) {
        if (humedad == null || humedad.signum() < 0 || humedad.compareTo(CIEN) > 0) {
            throw new IllegalArgumentException("La humedad debe estar entre 0 y 100");
        }
        if (humedad.compareTo(HUMEDAD_AUTORIZABLE) > 0) { return EstadoRecepcion.RECHAZADA; }
        return humedad.compareTo(HUMEDAD_NORMAL) > 0
            ? EstadoRecepcion.PENDIENTE : EstadoRecepcion.ACEPTADA;
    }
}
