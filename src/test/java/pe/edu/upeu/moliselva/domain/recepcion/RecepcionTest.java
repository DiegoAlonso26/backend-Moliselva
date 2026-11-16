package pe.edu.upeu.moliselva.domain.recepcion;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Comportamiento de la entidad Recepcion: ciclo de vida, reglas de edición e identidad. */
class RecepcionTest {
    @ParameterizedTest(name = "Humedad {0}: {1}, kardex={2}")
    @CsvSource({"14.5, ACEPTADA, true", "14.8, PENDIENTE, false", "15.01, RECHAZADA, false"})
    void registrarDecideElEstadoInicialPorLaHumedad(String humedad, EstadoRecepcion estado, boolean kardex) {
        var recepcion = nueva(humedad);

        assertNull(recepcion.id());
        assertEquals(estado, recepcion.estado());
        assertEquals(kardex, recepcion.ingresaAlKardex());
        assertEquals(new BigDecimal("880.000"), recepcion.entradaKardex().valor());
    }

    @ParameterizedTest
    @CsvSource({"ACEPTAR, ACEPTADA, true", "RECHAZAR, RECHAZADA, false"})
    void elJefeDecideUnaPendienteUnaSolaVez(DecisionRecepcion decision, EstadoRecepcion estado, boolean kardex) {
        var recepcion = guardada(EstadoRecepcion.PENDIENTE, null);

        recepcion.decidir(decision, 2);

        assertEquals(estado, recepcion.estado());
        assertEquals(2L, recepcion.idAutoriza());
        assertEquals(kardex, recepcion.ingresaAlKardex());
        assertFalse(recepcion.esEditable());
        var repetida = assertThrows(RecepcionInvalida.class, () -> recepcion.decidir(decision, 2));
        assertEquals(RecepcionInvalida.Regla.RECEPCION_NO_PENDIENTE, repetida.regla());
    }

    @Test
    void soloSeCorrigeSiNoEstaAceptadaNiDecidida() {
        var rechazadaAutomatica = guardada(EstadoRecepcion.RECHAZADA, null);
        rechazadaAutomatica.corregir(8, pesaje(), PorcentajeHumedad.de(new BigDecimal("14")));
        assertEquals(EstadoRecepcion.ACEPTADA, rechazadaAutomatica.estado());
        assertEquals(8, rechazadaAutomatica.idProveedor());

        for (var bloqueada : new Recepcion[]{guardada(EstadoRecepcion.ACEPTADA, null),
                guardada(EstadoRecepcion.RECHAZADA, 2L)}) {
            var error = assertThrows(RecepcionInvalida.class, () ->
                bloqueada.corregir(8, pesaje(), PorcentajeHumedad.de(new BigDecimal("14"))));
            assertEquals(RecepcionInvalida.Regla.RECEPCION_NO_EDITABLE, error.regla());
            assertEquals("La recepción aceptada o decidida no se puede modificar ni eliminar", error.getMessage());
            assertEquals(7, bloqueada.idProveedor());
        }
    }

    @Test
    void laIdentidadEsElIdAunqueCambienLosDatos() {
        var a = guardada(EstadoRecepcion.PENDIENTE, null);
        var b = guardada(EstadoRecepcion.PENDIENTE, null);
        b.decidir(DecisionRecepcion.RECHAZAR, 2);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(nueva("14.5"), nueva("14.5"));
        assertTrue(guardada(EstadoRecepcion.PENDIENTE, null).esEditable());
    }

    private static PesajeMaiz pesaje() {
        return PesajeMaiz.de(new BigDecimal("1000"), new BigDecimal("100"), new BigDecimal("10"), new BigDecimal("10"));
    }

    private static Recepcion nueva(String humedad) {
        return Recepcion.registrar(7, 4, 3, pesaje(), PorcentajeHumedad.de(new BigDecimal(humedad)));
    }

    private static Recepcion guardada(EstadoRecepcion estado, Long idAutoriza) {
        return Recepcion.existente(11, 7, 4, 3, pesaje(), PorcentajeHumedad.de(new BigDecimal("14.8")), estado, idAutoriza);
    }
}
