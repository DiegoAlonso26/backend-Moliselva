package pe.edu.upeu.moliselva.service;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BusinessRulesTest {
    @Test
    void recepcionCalculaNetoYExigeAutorizacionEnElRangoExcepcional() {
        assertEquals(new BigDecimal("890.00"), BusinessRules.pesoNeto(
            new BigDecimal("1000"), new BigDecimal("100"), new BigDecimal("10")));
        assertEquals("ACEPTADA", BusinessRules.estadoRecepcion(new BigDecimal("14.5")));
        assertEquals("PENDIENTE", BusinessRules.estadoRecepcion(new BigDecimal("14.8")));
        assertEquals("RECHAZADA", BusinessRules.estadoRecepcion(new BigDecimal("16")));
        assertThrows(IllegalArgumentException.class, () -> BusinessRules.estadoRecepcion(new BigDecimal("100.1")));
        assertThrows(IllegalArgumentException.class, () -> BusinessRules.pesoNeto(
            new BigDecimal("100"), new BigDecimal("100"), BigDecimal.ZERO));
        assertEquals(new BigDecimal("840.000"), BusinessRules.entradaKardex(
            new BigDecimal("890"), new BigDecimal("50")));
        assertThrows(IllegalArgumentException.class, () -> BusinessRules.entradaKardex(
            new BigDecimal("890"), new BigDecimal("890")));
    }

    @Test
    void loteCalculaMacrosParaTamanosPermitidos() {
        // El 1% aquí es una entrada matemática abstracta; no representa una fórmula de Moliselva.
        assertEquals(new BigDecimal("5.000"), BusinessRules.kgMacro(500, BigDecimal.ONE));
        assertEquals(new BigDecimal("10.000"), BusinessRules.kgMacro(1000, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class, () -> BusinessRules.kgMacro(750, BigDecimal.ONE));
    }

    @Test
    void despachoConvierteSacosAKilos() {
        assertEquals(new BigDecimal("80.000"), BusinessRules.kgDespacho(2, new BigDecimal("40")));
        assertThrows(IllegalArgumentException.class, () -> BusinessRules.kgDespacho(0, new BigDecimal("40")));
    }
}
