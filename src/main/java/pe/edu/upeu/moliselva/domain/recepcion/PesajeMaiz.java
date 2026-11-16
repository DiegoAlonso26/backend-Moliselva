package pe.edu.upeu.moliselva.domain.recepcion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import pe.edu.upeu.moliselva.domain.shared.Kilogramos;

/**
 * Value object: el pesaje de una recepción en la balanza (bruto, tara, sacos y merma de
 * limpieza). No tiene id; se valida al crearse, así que un pesaje que existe siempre es
 * coherente:
 * <ul>
 *   <li>peso neto = bruto - tara - sacos, con 2 decimales y mayor que cero;</li>
 *   <li>entrada al kardex = neto - merma, con 3 decimales; la merma es menor que el neto.</li>
 * </ul>
 */
public record PesajeMaiz(Kilogramos bruto, Kilogramos tara, Kilogramos sacos, Kilogramos merma) {
    static final String PESOS = "Los pesos deben ser válidos y no negativos";
    static final String NETO = "El peso neto debe ser positivo";
    static final String MERMA = "La merma de limpieza debe ser menor que el peso neto";

    public PesajeMaiz {
        if (bruto == null || tara == null || sacos == null || !bruto.esPositivo()) {
            throw pesosInvalidos(PESOS);
        }
        BigDecimal neto = neto(bruto.valor(), tara.valor(), sacos.valor());
        if (neto.signum() <= 0) {
            throw pesosInvalidos(NETO);
        }
        if (merma == null || merma.valor().compareTo(neto) >= 0) {
            throw pesosInvalidos(MERMA);
        }
    }

    /** Crea el pesaje desde los valores recibidos, validando en el mismo orden de siempre. */
    public static PesajeMaiz de(BigDecimal bruto, BigDecimal tara, BigDecimal sacos, BigDecimal merma) {
        if (bruto == null || tara == null || sacos == null || bruto.signum() <= 0
                || tara.signum() < 0 || sacos.signum() < 0) {
            throw pesosInvalidos(PESOS);
        }
        if (neto(bruto, tara, sacos).signum() <= 0) {
            throw pesosInvalidos(NETO);
        }
        if (merma == null || merma.signum() < 0) {
            throw pesosInvalidos(MERMA);
        }
        return new PesajeMaiz(Kilogramos.de(bruto), Kilogramos.de(tara), Kilogramos.de(sacos), Kilogramos.de(merma));
    }

    /** Peso neto en kg, con 2 decimales. */
    public Kilogramos neto() {
        return Kilogramos.de(neto(bruto.valor(), tara.valor(), sacos.valor()));
    }

    /** Kilos que ingresan al kardex: neto menos merma, con 3 decimales. */
    public Kilogramos entradaKardex() {
        return neto().menos(merma, 3);
    }

    private static BigDecimal neto(BigDecimal bruto, BigDecimal tara, BigDecimal sacos) {
        return bruto.subtract(tara).subtract(sacos).setScale(2, RoundingMode.HALF_UP);
    }

    private static RecepcionInvalida pesosInvalidos(String mensaje) {
        return new RecepcionInvalida(RecepcionInvalida.Regla.PESOS_INVALIDOS, mensaje);
    }
}
