package pe.edu.upeu.moliselva.application.inventario.kardex;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.moliselva.application.inventario.gateway.KardexMaizConsultaGateway;
import pe.edu.upeu.moliselva.application.inventario.kardex.exportar.ExportarKardexMaizCommand;
import pe.edu.upeu.moliselva.application.inventario.kardex.exportar.ExportarKardexMaizHandler;
import pe.edu.upeu.moliselva.application.inventario.kardex.listar.ListarKardexMaizHandler;
import pe.edu.upeu.moliselva.application.inventario.kardex.listar.ListarKardexMaizQuery;
import pe.edu.upeu.moliselva.application.inventario.kardex.stock.ConsultarStockMaizHandler;
import pe.edu.upeu.moliselva.application.inventario.kardex.stock.ConsultarStockMaizQuery;
import pe.edu.upeu.moliselva.application.shared.Actor;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.shared.FallaNegocio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class KardexMaizHandlersTest {
    private final KardexMaizConsultaGateway kardex = mock(KardexMaizConsultaGateway.class);
    private final BitacoraGateway bitacora = mock(BitacoraGateway.class);

    @Test
    void sinMaizConfiguradoDevuelveConflicto() {
        when(kardex.maizId()).thenReturn(Optional.empty());

        var error = assertThrows(MaizNoConfigurado.class, () ->
            new ConsultarStockMaizHandler(kardex).handle(new ConsultarStockMaizQuery()));
        assertThrows(MaizNoConfigurado.class, () -> new ListarKardexMaizHandler(kardex).handle(new ListarKardexMaizQuery()));

        assertEquals("MAIZ_NO_CONFIGURADO",error.codigo());
        assertEquals(FallaNegocio.Tipo.CONFLICTO,error.tipo());
    }

    @Test
    void stockDevuelveLosKilosDelMaiz() {
        when(kardex.maizId()).thenReturn(Optional.of(4L));
        when(kardex.stock(4)).thenReturn(new BigDecimal("880.000"));

        assertEquals(new BigDecimal("880.000"),
            new ConsultarStockMaizHandler(kardex).handle(new ConsultarStockMaizQuery()).stockKg());
    }

    @Test
    void exportarGeneraCsvParaExcelYRegistraBitacora() {
        when(kardex.maizId()).thenReturn(Optional.of(4L));
        when(kardex.movimientos(4)).thenReturn(List.of(new MovimientoKardexResponse(1,
            LocalDateTime.of(2026,11,16,9,30),"ENTRADA",new BigDecimal("790.000"),
            new BigDecimal("790.000"),12L,"operario_demo")));

        byte[] csv = new ExportarKardexMaizHandler(kardex,bitacora)
            .handle(new ExportarKardexMaizCommand(new Actor(3,"contadora","CONTADORA"))).csv();

        String texto = new String(csv,StandardCharsets.UTF_8);
        assertTrue(texto.startsWith("﻿idMovimiento;fechaHora;tipoMovimiento;cantidadKg;saldoKg;idRecepcion;usuario\r\n"));
        assertTrue(texto.contains("1;2026-11-16T09:30;ENTRADA;790.000;790.000;12;\"operario_demo\"\r\n"));
        verify(bitacora).registrar(3,"EXPORTAR_KARDEX_MAIZ",null);
    }

    @Test
    void operarioNoExportaNiDejaBitacora() {
        var error = assertThrows(FallaNegocio.class, () -> new ExportarKardexMaizHandler(kardex,bitacora)
            .handle(new ExportarKardexMaizCommand(new Actor(5,"operario","OPERARIO"))));

        assertEquals("SIN_PERMISO",error.codigo());
        verifyNoInteractions(kardex,bitacora);
    }
}
