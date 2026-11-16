package pe.edu.upeu.moliselva.domain.reglas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import pe.edu.upeu.moliselva.domain.recepcion.PesajeMaiz;
import pe.edu.upeu.moliselva.domain.recepcion.PorcentajeHumedad;
import pe.edu.upeu.moliselva.domain.produccion.LotePolicy;

/** Fachada de reglas sueltas; las de recepción delegan en los value objects PesajeMaiz y PorcentajeHumedad. */
public final class BusinessRules {
    private BusinessRules() {}

    public static BigDecimal pesoNeto(BigDecimal bruto, BigDecimal tara, BigDecimal sacos) {
        return PesajeMaiz.de(bruto,tara,sacos,BigDecimal.ZERO).neto().valor();
    }

    public static String estadoRecepcion(BigDecimal humedad) {
        return PorcentajeHumedad.de(humedad).estadoInicial().name();
    }

    public static BigDecimal entradaKardex(BigDecimal neto, BigDecimal mermaLimpieza) {
        return PesajeMaiz.de(neto,BigDecimal.ZERO,BigDecimal.ZERO,mermaLimpieza).entradaKardex().valor();
    }

    public static BigDecimal kgMacro(int tamanoKg, BigDecimal porcentaje) {
        return LotePolicy.kgMacro(tamanoKg,porcentaje);
    }

    public static BigDecimal kgDespacho(int sacos, BigDecimal pesoSacoKg) {
        if (sacos <= 0 || pesoSacoKg.signum() <= 0) throw new IllegalArgumentException("Sacos o peso por saco inválidos");
        return pesoSacoKg.multiply(BigDecimal.valueOf(sacos)).setScale(3, RoundingMode.HALF_UP);
    }
}
