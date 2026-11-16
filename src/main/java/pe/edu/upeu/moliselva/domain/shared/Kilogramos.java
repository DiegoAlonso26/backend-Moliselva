package pe.edu.upeu.moliselva.domain.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value object: una cantidad en kilogramos, nunca nula ni negativa. No tiene id: dos
 * instancias con el mismo valor son iguales (se compara con {@code compareTo}, así
 * 890.0 y 890.00 son el mismo peso).
 */
public record Kilogramos(BigDecimal valor) {
    public Kilogramos {
        if (valor == null || valor.signum() < 0) {
            throw new IllegalArgumentException("Los kilogramos no pueden ser nulos ni negativos");
        }
    }

    public static Kilogramos de(BigDecimal valor) { return new Kilogramos(valor); }

    public boolean esPositivo() { return valor.signum() > 0; }

    public boolean esMenorQue(Kilogramos otro) { return valor.compareTo(otro.valor) < 0; }

    public Kilogramos menos(Kilogramos otro, int decimales) {
        return new Kilogramos(valor.subtract(otro.valor).setScale(decimales, RoundingMode.HALF_UP));
    }

    @Override
    public boolean equals(Object otro) {
        return otro instanceof Kilogramos k && valor.compareTo(k.valor) == 0;
    }

    @Override
    public int hashCode() { return valor.stripTrailingZeros().hashCode(); }
}
