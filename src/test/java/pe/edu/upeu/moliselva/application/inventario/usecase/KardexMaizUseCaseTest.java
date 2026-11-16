package pe.edu.upeu.moliselva.application.inventario.usecase;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.moliselva.application.inventario.gateway.KardexMaizGateway;
import pe.edu.upeu.moliselva.application.shared.Actor;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.shared.FallaNegocio;
import pe.edu.upeu.moliselva.domain.inventario.MovimientoKardex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class KardexMaizUseCaseTest {
    private final KardexMaizGateway kardex = mock(KardexMaizGateway.class);
    private final BitacoraGateway bitacora = mock(BitacoraGateway.class);
    private final ConsultarKardexMaiz consultar = new ConsultarKardexMaiz(kardex);

    @Test
    void sinMaizConfiguradoDevuelveConflicto() {
        when(kardex.maizId()).thenReturn(Optional.empty());

        var error = assertThrows(MaizNoConfigurado.class, consultar::stockKg);

        assertEquals("MAIZ_NO_CONFIGURADO",error.codigo());
        assertEquals(FallaNegocio.Tipo.CONFLICTO,error.tipo());
    }

    @Test
    void exportarGeneraCsvParaExcelYRegistraBitacora() {
        when(kardex.maizId()).thenReturn(Optional.of(4L));
        when(kardex.movimientos(4)).thenReturn(List.of(new MovimientoKardex(1,
            LocalDateTime.of(2026,11,16,9,30),"ENTRADA",new BigDecimal("790.000"),
            new BigDecimal("790.000"),12L,"operario_demo")));

        byte[] csv = new ExportarKardexMaiz(consultar,bitacora).exportar(new Actor(3,"contadora","CONTADORA"));

        String texto = new String(csv,StandardCharsets.UTF_8);
        assertTrue(texto.startsWith("﻿idMovimiento;fechaHora;tipoMovimiento;cantidadKg;saldoKg;idRecepcion;usuario\r\n"));
        assertTrue(texto.contains("1;2026-11-16T09:30;ENTRADA;790.000;790.000;12;\"operario_demo\"\r\n"));
        verify(bitacora).registrar(3,"EXPORTAR_KARDEX_MAIZ",null);
    }

    @Test
    void operarioNoExportaNiDejaBitacora() {
        var error = assertThrows(FallaNegocio.class, () ->
            new ExportarKardexMaiz(consultar,bitacora).exportar(new Actor(5,"operario","OPERARIO")));

        assertEquals("SIN_PERMISO",error.codigo());
        verifyNoInteractions(kardex,bitacora);
    }
}
