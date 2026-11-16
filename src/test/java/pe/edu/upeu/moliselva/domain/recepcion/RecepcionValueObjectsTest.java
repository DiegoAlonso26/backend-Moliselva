package pe.edu.upeu.moliselva.domain.recepcion;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import pe.edu.upeu.moliselva.domain.shared.Kilogramos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Value objects de la recepción: los mismos casos límite que tenía RecepcionPolicy
 * (humedad, peso neto y merma), ahora validados al crear PorcentajeHumedad y PesajeMaiz.
 */
class RecepcionValueObjectsTest {
    @ParameterizedTest(name = "Humedad {0}: {1}")
    @CsvSource({
        "0, ACEPTADA", "14.49, ACEPTADA", "14.50, ACEPTADA",
        "14.51, PENDIENTE", "14.99, PENDIENTE", "15.00, PENDIENTE",
        "15.01, RECHAZADA", "100, RECHAZADA"
    })
    void clasificaHumedadEnLosLimites(String humedad, EstadoRecepcion esperado) {
        assertEquals(esperado, PorcentajeHumedad.de(new BigDecimal(humedad)).estadoInicial());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"-0.01", "100.01"})
    void rechazaHumedadInvalida(String humedad) {
        BigDecimal valor = humedad == null ? null : new BigDecimal(humedad);
        var error = assertThrows(RecepcionInvalida.class, () -> PorcentajeHumedad.de(valor));
        assertEquals(RecepcionInvalida.Regla.HUMEDAD_FUERA_DE_RANGO, error.regla());
    }

    @ParameterizedTest
    @CsvSource({"1000, 100, 10, 890.00", "1000, 0, 0, 1000.00",
        "1000, 0, 10, 990.00", "1000, 100, 0, 900.00"})
    void calculaPesoNetoConDosDecimales(String bruto, String tara, String sacos, String esperado) {
        BigDecimal neto = PesajeMaiz.de(new BigDecimal(bruto), new BigDecimal(tara), new BigDecimal(sacos),
            BigDecimal.ZERO).neto().valor();
        assertEquals(new BigDecimal(esperado), neto);
        assertEquals(2, neto.scale());
    }

    @ParameterizedTest
    @CsvSource({"100, 90, 10", "100, 100, 1", "0, 0, 0",
        "-1, 0, 0", "100, -1, 0", "100, 0, -1"})
    void rechazaPesosInvalidosONetoNoPositivo(String bruto, String tara, String sacos) {
        var error = assertThrows(RecepcionInvalida.class, () -> PesajeMaiz.de(
            new BigDecimal(bruto), new BigDecimal(tara), new BigDecimal(sacos), BigDecimal.ZERO));
        assertEquals(RecepcionInvalida.Regla.PESOS_INVALIDOS, error.regla());
    }

    @ParameterizedTest
    @CsvSource({"890, 50, 840.000", "890, 0, 890.000", "890, 889.999, 0.001"})
    void descuentaMermaConTresDecimales(String neto, String merma, String esperado) {
        BigDecimal entrada = PesajeMaiz.de(new BigDecimal(neto), BigDecimal.ZERO, BigDecimal.ZERO,
            new BigDecimal(merma)).entradaKardex().valor();
        assertEquals(new BigDecimal(esperado), entrada);
        assertEquals(3, entrada.scale());
    }

    @ParameterizedTest
    @ValueSource(strings = {"890", "891", "-0.001"})
    void rechazaMermaInvalida(String merma) {
        var error = assertThrows(RecepcionInvalida.class, () ->
            PesajeMaiz.de(new BigDecimal("890"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal(merma)));
        assertEquals("La merma de limpieza debe ser menor que el peso neto", error.getMessage());
    }

    @Test
    void evaluaPesoEntradaYEstadoEnConjunto() {
        var pesaje = PesajeMaiz.de(new BigDecimal("1000"), new BigDecimal("100"),
            new BigDecimal("10"), new BigDecimal("10"));

        assertEquals(new BigDecimal("890.00"), pesaje.neto().valor());
        assertEquals(new BigDecimal("880.000"), pesaje.entradaKardex().valor());
        assertEquals(EstadoRecepcion.ACEPTADA, PorcentajeHumedad.de(new BigDecimal("14.5")).estadoInicial());
    }

    @Test
    void losValueObjectsSeComparanPorValorYNoTienenId() {
        assertEquals(Kilogramos.de(new BigDecimal("890.0")), Kilogramos.de(new BigDecimal("890.00")));
        assertEquals(Kilogramos.de(new BigDecimal("890.0")).hashCode(), Kilogramos.de(new BigDecimal("890.00")).hashCode());
        assertEquals(PorcentajeHumedad.de(new BigDecimal("14.5")), PorcentajeHumedad.de(new BigDecimal("14.50")));
        assertEquals(
            PesajeMaiz.de(new BigDecimal("1000"), new BigDecimal("100"), new BigDecimal("10"), new BigDecimal("10")),
            PesajeMaiz.de(new BigDecimal("1000.00"), new BigDecimal("100"), new BigDecimal("10.0"), new BigDecimal("10")));
        assertNotEquals(Kilogramos.de(BigDecimal.ONE), Kilogramos.de(BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> Kilogramos.de(new BigDecimal("-1")));
    }
}
