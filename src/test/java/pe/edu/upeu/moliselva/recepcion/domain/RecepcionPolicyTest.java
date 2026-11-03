package pe.edu.upeu.moliselva.recepcion.domain;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecepcionPolicyTest {
    @ParameterizedTest(name = "Humedad {0}: {1}")
    @CsvSource({
        "0, ACEPTADA", "14.49, ACEPTADA", "14.50, ACEPTADA",
        "14.51, PENDIENTE", "14.99, PENDIENTE", "15.00, PENDIENTE",
        "15.01, RECHAZADA", "100, RECHAZADA"
    })
    void clasificaHumedadEnLosLimites(String humedad, EstadoRecepcion esperado) {
        assertEquals(esperado, RecepcionPolicy.estadoRecepcion(new BigDecimal(humedad)));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"-0.01", "100.01"})
    void rechazaHumedadInvalida(String humedad) {
        BigDecimal valor = humedad == null ? null : new BigDecimal(humedad);
        assertThrows(IllegalArgumentException.class, () -> RecepcionPolicy.estadoRecepcion(valor));
    }

    @ParameterizedTest
    @CsvSource({"1000, 100, 10, 890.00", "1000, 0, 0, 1000.00",
        "1000, 0, 10, 990.00", "1000, 100, 0, 900.00"})
    void calculaPesoNetoConDosDecimales(String bruto, String tara, String sacos, String esperado) {
        BigDecimal neto = RecepcionPolicy.pesoNeto(new BigDecimal(bruto),
            new BigDecimal(tara), new BigDecimal(sacos));
        assertEquals(new BigDecimal(esperado), neto);
        assertEquals(2, neto.scale());
    }

    @ParameterizedTest
    @CsvSource({"100, 90, 10", "100, 100, 1", "0, 0, 0",
        "-1, 0, 0", "100, -1, 0", "100, 0, -1"})
    void rechazaPesosInvalidosONetoNoPositivo(String bruto, String tara, String sacos) {
        assertThrows(IllegalArgumentException.class, () -> RecepcionPolicy.pesoNeto(
            new BigDecimal(bruto), new BigDecimal(tara), new BigDecimal(sacos)));
    }

    @ParameterizedTest
    @CsvSource({"890, 50, 840.000", "890, 0, 890.000", "890, 889.999, 0.001"})
    void descuentaMermaConTresDecimales(String neto, String merma, String esperado) {
        BigDecimal entrada = RecepcionPolicy.entradaKardex(new BigDecimal(neto), new BigDecimal(merma));
        assertEquals(new BigDecimal(esperado), entrada);
        assertEquals(3, entrada.scale());
    }

    @ParameterizedTest
    @ValueSource(strings = {"890", "891", "-0.001"})
    void rechazaMermaInvalida(String merma) {
        assertThrows(IllegalArgumentException.class, () ->
            RecepcionPolicy.entradaKardex(new BigDecimal("890"), new BigDecimal(merma)));
    }

    @Test
    void evaluaPesoEntradaYEstadoEnConjunto() {
        var datos = new RecepcionDatos(7, new BigDecimal("1000"), new BigDecimal("100"),
            new BigDecimal("10"), new BigDecimal("14.5"), new BigDecimal("10"));
        var resultado = RecepcionPolicy.evaluar(datos);

        assertEquals(new BigDecimal("890.00"), resultado.pesoNetoKg());
        assertEquals(new BigDecimal("880.000"), resultado.entradaKardexKg());
        assertEquals(EstadoRecepcion.ACEPTADA, resultado.estado());
    }
}
