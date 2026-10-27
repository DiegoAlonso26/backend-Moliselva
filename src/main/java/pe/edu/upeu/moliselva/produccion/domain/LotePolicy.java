package pe.edu.upeu.moliselva.produccion.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class LotePolicy {
    private LotePolicy() {}

    public static boolean tamanoValido(int tamanoKg) {
        return tamanoKg == 500 || tamanoKg == 1000;
    }

    public static BigDecimal kgMacro(int tamanoKg, BigDecimal porcentaje) {
        if (!tamanoValido(tamanoKg)) {
            throw new IllegalArgumentException("El lote debe ser de 500 o 1000 kg");
        }
        if (porcentaje == null || porcentaje.signum() <= 0 ||
                porcentaje.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Porcentaje de macro inválido");
        }
        return BigDecimal.valueOf(tamanoKg).multiply(porcentaje)
            .divide(new BigDecimal("100"),3,RoundingMode.HALF_UP);
    }

    public static boolean editable(Lote lote, boolean tieneMovimientos) {
        return lote.estado() == EstadoLote.PLANIFICADO && !tieneMovimientos;
    }

    public static boolean insumoCorregible(Lote lote, boolean tieneMovimientos) {
        return (lote.estado() == EstadoLote.PLANIFICADO ||
            lote.estado() == EstadoLote.EN_PROCESO) && !tieneMovimientos;
    }
}
