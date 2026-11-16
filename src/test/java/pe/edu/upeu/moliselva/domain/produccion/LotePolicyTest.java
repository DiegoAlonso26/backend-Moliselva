package pe.edu.upeu.moliselva.domain.produccion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Reglas de lotes: tamaños de 500 o 1000 kg, cálculo de kg por porcentaje y estados editables. */
class LotePolicyTest {

    @ParameterizedTest
    @ValueSource(ints = {500, 1000})
    void tamanosPermitidos(int kg) { assertTrue(LotePolicy.tamanoValido(kg)); }

    @ParameterizedTest
    @ValueSource(ints = {0, 250, 499, 501, 750, 999, 1001, 2000})
    void tamanosNoPermitidos(int kg) { assertFalse(LotePolicy.tamanoValido(kg)); }

    @ParameterizedTest
    @CsvSource({"1000,50,500.000", "500,10,50.000", "1000,0.125,1.250", "500,100,500.000"})
    void kgPorPorcentajeSeRedondeaATresDecimales(int tamano, String porcentaje, String esperado) {
        assertEquals(new BigDecimal(esperado), LotePolicy.kgMacro(tamano, new BigDecimal(porcentaje)));
    }

    @Test
    void kgPorPorcentajeRechazaDatosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> LotePolicy.kgMacro(750, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> LotePolicy.kgMacro(500, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> LotePolicy.kgMacro(500, new BigDecimal("100.01")));
        assertThrows(IllegalArgumentException.class, () -> LotePolicy.kgMacro(500, null));
    }

    @Test
    void soloElLotePlanificadoSinMovimientosEsEditable() {
        assertTrue(LotePolicy.editable(lote(EstadoLote.PLANIFICADO), false));
        assertFalse(LotePolicy.editable(lote(EstadoLote.PLANIFICADO), true));
    }

    @ParameterizedTest
    @EnumSource(value = EstadoLote.class, names = {"EN_PROCESO", "TERMINADO", "ANULADO"})
    void otrosEstadosNoSonEditables(EstadoLote estado) {
        assertFalse(LotePolicy.editable(lote(estado), false));
    }

    @ParameterizedTest
    @EnumSource(value = EstadoLote.class, names = {"PLANIFICADO", "EN_PROCESO"})
    void insumosCorregiblesAntesDeTerminarYSinMovimientos(EstadoLote estado) {
        assertTrue(LotePolicy.insumoCorregible(lote(estado), false));
        assertFalse(LotePolicy.insumoCorregible(lote(estado), true));
    }

    @ParameterizedTest
    @EnumSource(value = EstadoLote.class, names = {"TERMINADO", "ANULADO"})
    void insumosNoCorregiblesEnLoteCerrado(EstadoLote estado) {
        assertFalse(LotePolicy.insumoCorregible(lote(estado), false));
    }

    private static Lote lote(EstadoLote estado) {
        return new Lote(1, 2, 3, LocalDateTime.of(2026, 11, 16, 8, 0), 1000, estado, 4);
    }
}
