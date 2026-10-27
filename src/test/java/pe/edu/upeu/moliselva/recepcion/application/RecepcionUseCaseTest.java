package pe.edu.upeu.moliselva.recepcion.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.moliselva.recepcion.application.port.out.BitacoraPort;
import pe.edu.upeu.moliselva.recepcion.application.port.out.InventarioPort;
import pe.edu.upeu.moliselva.recepcion.application.port.out.ProveedorPort;
import pe.edu.upeu.moliselva.recepcion.application.port.out.RecepcionPort;
import pe.edu.upeu.moliselva.recepcion.domain.DecisionRecepcion;
import pe.edu.upeu.moliselva.recepcion.domain.EstadoRecepcion;
import pe.edu.upeu.moliselva.recepcion.domain.Recepcion;
import pe.edu.upeu.moliselva.recepcion.domain.RecepcionBloqueo;
import pe.edu.upeu.moliselva.recepcion.domain.RecepcionDatos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RecepcionUseCaseTest {
    private final RecepcionPort recepciones = mock(RecepcionPort.class);
    private final ProveedorPort proveedores = mock(ProveedorPort.class);
    private final InventarioPort inventario = mock(InventarioPort.class);
    private final BitacoraPort bitacora = mock(BitacoraPort.class);
    private final ActorRecepcion operario = new ActorRecepcion(3,"operario","OPERARIO");
    private final ActorRecepcion jefe = new ActorRecepcion(2,"jefe","JEFE_PLANTA");

    @Test
    void recepcionAceptadaRegistraUnaEntradaYAuditoria() {
        RecepcionDatos datos = datos("14.5");
        when(proveedores.existeActivo(7)).thenReturn(true);
        when(inventario.maizId()).thenReturn(Optional.of(4L));
        when(recepciones.insertar(datos,4,EstadoRecepcion.ACEPTADA)).thenReturn(11L);
        when(recepciones.buscar(11)).thenReturn(Optional.of(recepcion(EstadoRecepcion.ACEPTADA)));

        Recepcion resultado = new RegistrarRecepcion(recepciones,proveedores,inventario,bitacora)
            .registrar(datos,operario);

        assertEquals("operario",resultado.registradoPor());
        verify(inventario).registrarEntrada(4,11,new BigDecimal("880.000"),3);
        verify(bitacora).registrar(3,"REGISTRAR_RECEPCION id=11 estado=ACEPTADA",null);
    }

    @Test
    void recepcionPendienteNoIngresaAlKardex() {
        RecepcionDatos datos = datos("14.8");
        when(proveedores.existeActivo(7)).thenReturn(true);
        when(inventario.maizId()).thenReturn(Optional.of(4L));
        when(recepciones.insertar(datos,4,EstadoRecepcion.PENDIENTE)).thenReturn(11L);
        when(recepciones.buscar(11)).thenReturn(Optional.of(recepcion(EstadoRecepcion.PENDIENTE)));

        new RegistrarRecepcion(recepciones,proveedores,inventario,bitacora)
            .registrar(datos,operario);

        verify(inventario,never()).registrarEntrada(anyLong(),anyLong(),any(),anyLong());
        verify(bitacora).registrar(3,"REGISTRAR_RECEPCION id=11 estado=PENDIENTE",null);
    }

    @Test
    void decisionAceptadaIngresaUnaVezYRepetirlaFalla() {
        var bloqueo = new RecepcionBloqueo(4,new BigDecimal("890.00"),new BigDecimal("10"),
            EstadoRecepcion.PENDIENTE,null);
        when(recepciones.bloquear(11)).thenReturn(Optional.of(bloqueo),
            Optional.of(new RecepcionBloqueo(4,new BigDecimal("890.00"),new BigDecimal("10"),
                EstadoRecepcion.ACEPTADA,2L)));
        when(recepciones.decidir(11,EstadoRecepcion.ACEPTADA,2)).thenReturn(true);
        when(recepciones.buscar(11)).thenReturn(Optional.of(recepcion(EstadoRecepcion.ACEPTADA)));
        var caso = new DecidirRecepcion(recepciones,inventario,bitacora);

        caso.decidir(11,DecisionRecepcion.ACEPTAR,"Verificado",jefe);
        RecepcionFailure error = assertThrows(RecepcionFailure.class,
            () -> caso.decidir(11,DecisionRecepcion.ACEPTAR,"Verificado",jefe));

        assertEquals(RecepcionFailure.Reason.RECEPCION_NO_PENDIENTE,error.reason());
        verify(inventario).registrarEntrada(4,11,new BigDecimal("880.000"),2);
        verify(bitacora).registrar(2,"DECISION_RECEPCION id=11 estado=ACEPTADA","Verificado");
    }

    private static RecepcionDatos datos(String humedad) {
        return new RecepcionDatos(7,new BigDecimal("1000"),new BigDecimal("100"),
            new BigDecimal("10"),new BigDecimal(humedad),new BigDecimal("10"));
    }

    private static Recepcion recepcion(EstadoRecepcion estado) {
        return new Recepcion(11,LocalDateTime.of(2026,10,27,12,0),7,"Proveedor",
            new BigDecimal("1000"),new BigDecimal("100"),new BigDecimal("10"),
            new BigDecimal("890.00"),new BigDecimal("14.5"),new BigDecimal("10"),
            estado,null,null,null);
    }
}
