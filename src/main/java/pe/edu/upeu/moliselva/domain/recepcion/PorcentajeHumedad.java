package pe.edu.upeu.moliselva.domain.recepcion;

import java.math.BigDecimal;

/**
 * Value object: humedad del maíz en porcentaje, entre 0 y 100. Decide el estado inicial
 * de la recepción:
 * <ul>
 *   <li>hasta 14.5 %: ACEPTADA;</li>
 *   <li>más de 14.5 % y hasta 15 %: PENDIENTE (la decide el jefe de planta);</li>
 *   <li>más de 15 %: RECHAZADA.</li>
 * </ul>
 */
public record PorcentajeHumedad(BigDecimal valor) {
    private static final BigDecimal HUMEDAD_NORMAL = new BigDecimal("14.5");
    private static final BigDecimal HUMEDAD_AUTORIZABLE = new BigDecimal("15");
    private static final BigDecimal CIEN = new BigDecimal("100");

    public PorcentajeHumedad {
        if (valor == null || valor.signum() < 0 || valor.compareTo(CIEN) > 0) {
            throw new RecepcionInvalida(RecepcionInvalida.Regla.HUMEDAD_FUERA_DE_RANGO,
                "La humedad debe estar entre 0 y 100");
        }
    }

    public static PorcentajeHumedad de(BigDecimal valor) { return new PorcentajeHumedad(valor); }

    public EstadoRecepcion estadoInicial() {
        if (valor.compareTo(HUMEDAD_AUTORIZABLE) > 0) { return EstadoRecepcion.RECHAZADA; }
        return valor.compareTo(HUMEDAD_NORMAL) > 0 ? EstadoRecepcion.PENDIENTE : EstadoRecepcion.ACEPTADA;
    }

    @Override
    public boolean equals(Object otro) {
        return otro instanceof PorcentajeHumedad h && valor.compareTo(h.valor) == 0;
    }

    @Override
    public int hashCode() { return valor.stripTrailingZeros().hashCode(); }
}
