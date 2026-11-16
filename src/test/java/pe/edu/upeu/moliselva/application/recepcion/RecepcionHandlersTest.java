package pe.edu.upeu.moliselva.application.recepcion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import pe.edu.upeu.moliselva.application.recepcion.corregir.CorregirRecepcionCommand;
import pe.edu.upeu.moliselva.application.recepcion.corregir.CorregirRecepcionHandler;
import pe.edu.upeu.moliselva.application.recepcion.decidir.DecidirRecepcionCommand;
import pe.edu.upeu.moliselva.application.recepcion.decidir.DecidirRecepcionHandler;
import pe.edu.upeu.moliselva.application.recepcion.eliminar.EliminarRecepcionCommand;
import pe.edu.upeu.moliselva.application.recepcion.eliminar.EliminarRecepcionHandler;
import pe.edu.upeu.moliselva.application.recepcion.gateway.EntradaKardexGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.ProveedorActivoGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.RecepcionConsultaGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.RecepcionGateway;
import pe.edu.upeu.moliselva.application.recepcion.obtener.ObtenerRecepcionHandler;
import pe.edu.upeu.moliselva.application.recepcion.obtener.ObtenerRecepcionQuery;
import pe.edu.upeu.moliselva.application.recepcion.registrar.RegistrarRecepcionCommand;
import pe.edu.upeu.moliselva.application.recepcion.registrar.RegistrarRecepcionHandler;
import pe.edu.upeu.moliselva.application.shared.Actor;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.domain.recepcion.DecisionRecepcion;
import pe.edu.upeu.moliselva.domain.recepcion.EstadoRecepcion;
import pe.edu.upeu.moliselva.domain.recepcion.PesajeMaiz;
import pe.edu.upeu.moliselva.domain.recepcion.PorcentajeHumedad;
import pe.edu.upeu.moliselva.domain.recepcion.Recepcion;
import pe.edu.upeu.moliselva.domain.shared.Kilogramos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Handlers de recepción con gateways simulados: los mismos casos y expectativas que tenían
 * los casos de uso antes de pasar a Command/Handler.
 */
class RecepcionHandlersTest {
    private final RecepcionGateway recepciones = mock(RecepcionGateway.class);
    private final RecepcionConsultaGateway consultas = mock(RecepcionConsultaGateway.class);
    private final ProveedorActivoGateway proveedores = mock(ProveedorActivoGateway.class);
    private final EntradaKardexGateway kardex = mock(EntradaKardexGateway.class);
    private final BitacoraGateway bitacora = mock(BitacoraGateway.class);
    private final Actor operario = new Actor(3,"operario","OPERARIO");
    private final Actor jefe = new Actor(2,"jefe","JEFE_PLANTA");

    private RegistrarRecepcionHandler registrar() {
        return new RegistrarRecepcionHandler(recepciones,consultas,proveedores,kardex,bitacora);
    }

    private DecidirRecepcionHandler decidir() {
        return new DecidirRecepcionHandler(recepciones,consultas,kardex,bitacora);
    }

    @Test
    void recepcionAceptadaRegistraUnaEntradaYAuditoria() {
        prepararRegistro(11);
        when(consultas.buscar(11)).thenReturn(Optional.of(respuesta(EstadoRecepcion.ACEPTADA)));

        RecepcionResponse resultado = registrar().handle(registro("14.5",operario));

        assertEquals("operario",resultado.registradoPor());
        verify(kardex,times(1)).registrarEntrada(4,11,Kilogramos.de(new BigDecimal("880.000")),3);
        verify(bitacora).registrar(3,"REGISTRAR_RECEPCION id=11 estado=ACEPTADA",null);
    }

    @Test
    void recepcionPendienteNoIngresaAlKardex() {
        prepararRegistro(11);
        when(consultas.buscar(11)).thenReturn(Optional.of(respuesta(EstadoRecepcion.PENDIENTE)));

        registrar().handle(registro("14.8",operario));

        verify(kardex,never()).registrarEntrada(anyLong(),anyLong(),any(),anyLong());
        verify(bitacora).registrar(3,"REGISTRAR_RECEPCION id=11 estado=PENDIENTE",null);
    }

    @Test
    void decisionAceptadaIngresaUnaVezYRepetirlaFalla() {
        when(recepciones.bloquear(11)).thenReturn(Optional.of(guardada(EstadoRecepcion.PENDIENTE,null)),
            Optional.of(guardada(EstadoRecepcion.ACEPTADA,2L)));
        when(consultas.buscar(11)).thenReturn(Optional.of(respuesta(EstadoRecepcion.ACEPTADA)));
        var handler = decidir();

        handler.handle(decision(DecisionRecepcion.ACEPTAR,"Verificado"));
        RecepcionFailure error = assertThrows(RecepcionFailure.class,
            () -> handler.handle(decision(DecisionRecepcion.ACEPTAR,"Verificado")));

        assertEquals(RecepcionFailure.Reason.RECEPCION_NO_PENDIENTE,error.reason());
        verify(recepciones,times(1)).guardar(any());
        verify(kardex).registrarEntrada(4,11,Kilogramos.de(new BigDecimal("880.000")),2);
        verify(bitacora).registrar(2,"DECISION_RECEPCION id=11 estado=ACEPTADA","Verificado");
    }

    @Test
    void decisionGuardaElEstadoYElJefeQueAutoriza() {
        when(recepciones.bloquear(11)).thenReturn(Optional.of(guardada(EstadoRecepcion.PENDIENTE,null)));
        when(consultas.buscar(11)).thenReturn(Optional.of(respuesta(EstadoRecepcion.ACEPTADA)));

        decidir().handle(decision(DecisionRecepcion.ACEPTAR,"  Verificado  "));

        var guardada = ArgumentCaptor.forClass(Recepcion.class);
        verify(recepciones).guardar(guardada.capture());
        assertEquals(EstadoRecepcion.ACEPTADA,guardada.getValue().estado());
        assertEquals(2L,guardada.getValue().idAutoriza());
        verify(bitacora).registrar(2,"DECISION_RECEPCION id=11 estado=ACEPTADA","Verificado");
    }

    @Test
    void recepcionRechazadaNoIngresaAlKardex() {
        prepararRegistro(11);
        when(consultas.buscar(11)).thenReturn(Optional.of(respuesta(EstadoRecepcion.RECHAZADA)));

        var resultado = registrar().handle(registro("15.01",operario));

        assertEquals(EstadoRecepcion.RECHAZADA,resultado.estado());
        verify(kardex,never()).registrarEntrada(anyLong(),anyLong(),any(),anyLong());
        verify(bitacora).registrar(3,"REGISTRAR_RECEPCION id=11 estado=RECHAZADA",null);
    }

    @Test
    void rechazoDelJefeConservaMotivoSinEntradaAlKardex() {
        when(recepciones.bloquear(11)).thenReturn(Optional.of(guardada(EstadoRecepcion.PENDIENTE,null)));
        when(consultas.buscar(11)).thenReturn(Optional.of(respuesta(EstadoRecepcion.RECHAZADA)));

        var resultado = decidir().handle(decision(DecisionRecepcion.RECHAZAR,"Revisión de prueba"));

        assertEquals(EstadoRecepcion.RECHAZADA,resultado.estado());
        verify(kardex,never()).registrarEntrada(anyLong(),anyLong(),any(),anyLong());
        verify(bitacora).registrar(2,"DECISION_RECEPCION id=11 estado=RECHAZADA","Revisión de prueba");
    }

    @Test
    void operarioNoPuedeDecidirNiModificarInventario() {
        var error = assertThrows(RecepcionFailure.class, () ->
            decidir().handle(new DecidirRecepcionCommand(11,DecisionRecepcion.ACEPTAR,"Revisión de prueba",operario)));

        assertEquals(RecepcionFailure.Reason.SIN_PERMISO,error.reason());
        verifyNoInteractions(recepciones,consultas,kardex,bitacora);
    }

    @ParameterizedTest
    @EnumSource(value = EstadoRecepcion.class, names = {"ACEPTADA", "RECHAZADA"})
    void correccionDeRecepcionDecididaFallaSinEfectos(EstadoRecepcion estado) {
        when(recepciones.bloquear(11)).thenReturn(Optional.of(guardada(estado,2L)));

        var error = assertThrows(RecepcionFailure.class, () ->
            new CorregirRecepcionHandler(recepciones,consultas,proveedores,kardex,bitacora)
                .handle(correccion("14.5",jefe)));

        assertEquals(RecepcionFailure.Reason.RECEPCION_NO_EDITABLE,error.reason());
        verify(recepciones,never()).guardar(any());
        verifyNoInteractions(proveedores,kardex,bitacora);
    }

    @Test
    void correccionQueAceptaLaRecepcionCreaLaEntradaAlKardex() {
        when(recepciones.bloquear(11)).thenReturn(Optional.of(guardada(EstadoRecepcion.PENDIENTE,null)));
        when(proveedores.existeActivo(7)).thenReturn(true);
        when(consultas.buscar(11)).thenReturn(Optional.of(respuesta(EstadoRecepcion.ACEPTADA)));

        new CorregirRecepcionHandler(recepciones,consultas,proveedores,kardex,bitacora).handle(correccion("14.5",operario));

        verify(recepciones).guardar(any());
        verify(kardex).registrarEntrada(4,11,Kilogramos.de(new BigDecimal("880.000")),3);
        verify(bitacora).registrar(3,"ACTUALIZAR_RECEPCION id=11 estado=ACEPTADA",null);
    }

    @ParameterizedTest
    @EnumSource(value = EstadoRecepcion.class, names = {"ACEPTADA", "RECHAZADA"})
    void eliminacionDeRecepcionDecididaFallaSinEfectos(EstadoRecepcion estado) {
        when(recepciones.bloquear(11)).thenReturn(Optional.of(guardada(estado,2L)));

        var error = assertThrows(RecepcionFailure.class, () ->
            new EliminarRecepcionHandler(recepciones,bitacora).handle(new EliminarRecepcionCommand(11,jefe)));

        assertEquals(RecepcionFailure.Reason.RECEPCION_NO_EDITABLE,error.reason());
        verify(recepciones,never()).eliminar(any());
        verifyNoInteractions(kardex,bitacora);
    }

    @Test
    void registrarEnviaElIdDelOperarioComoIdRegistra() {
        prepararRegistro(11);
        when(consultas.buscar(11)).thenReturn(Optional.of(respuesta(EstadoRecepcion.ACEPTADA)));

        registrar().handle(registro("14.5",operario));

        var nueva = agregada();
        assertEquals(3,nueva.idRegistra());
        assertEquals(4,nueva.idInsumo());
        assertEquals(EstadoRecepcion.ACEPTADA,nueva.estado());
    }

    @Test
    void registrarEnviaElIdDelJefeDePlantaComoIdRegistra() {
        when(proveedores.existeActivo(7)).thenReturn(true);
        when(kardex.maizId()).thenReturn(Optional.of(4L));
        when(recepciones.agregar(any())).thenReturn(12L);
        when(consultas.buscar(12)).thenReturn(Optional.of(respuesta(EstadoRecepcion.PENDIENTE)));

        var resultado = registrar().handle(registro("14.8",jefe));

        var nueva = agregada();
        assertEquals(2,nueva.idRegistra());
        assertEquals(EstadoRecepcion.PENDIENTE,nueva.estado());
        assertEquals("jefe",resultado.registradoPor());
    }

    @Test
    void rolSinPermisoNoRegistraNiLlamaAInsertar() {
        var contadora = new Actor(4,"contadora","CONTADORA");

        var error = assertThrows(RecepcionFailure.class, () -> registrar().handle(registro("14.5",contadora)));

        assertEquals(RecepcionFailure.Reason.SIN_PERMISO,error.reason());
        verify(recepciones,never()).agregar(any());
        verifyNoInteractions(proveedores,kardex,bitacora);
    }

    @Test
    void elProveedorSeCompruebaAntesQueLosPesos() {
        when(proveedores.existeActivo(7)).thenReturn(false);
        var pesosInvalidos = new RegistrarRecepcionCommand(7,new BigDecimal("100"),new BigDecimal("200"),
            BigDecimal.ZERO,new BigDecimal("14"),BigDecimal.ZERO,operario);

        var error = assertThrows(RecepcionFailure.class, () -> registrar().handle(pesosInvalidos));

        assertEquals(RecepcionFailure.Reason.PROVEEDOR_INVALIDO,error.reason());
        verifyNoInteractions(recepciones,kardex,bitacora);
    }

    @Test
    void reglasDelDominioSalenConSuCodigoYMensaje() {
        when(proveedores.existeActivo(7)).thenReturn(true);
        when(kardex.maizId()).thenReturn(Optional.of(4L));

        var pesos = assertThrows(RecepcionFailure.class, () -> registrar().handle(new RegistrarRecepcionCommand(7,
            new BigDecimal("100"),new BigDecimal("200"),BigDecimal.ZERO,new BigDecimal("14"),BigDecimal.ZERO,operario)));
        var humedad = assertThrows(RecepcionFailure.class, () -> registrar().handle(registro("100.01",operario)));

        assertEquals(RecepcionFailure.Reason.PESOS_INVALIDOS,pesos.reason());
        assertEquals("El peso neto debe ser positivo",pesos.getMessage());
        assertEquals(RecepcionFailure.Reason.HUMEDAD_FUERA_DE_RANGO,humedad.reason());
        assertEquals("La humedad debe estar entre 0 y 100",humedad.getMessage());
        verify(recepciones,never()).agregar(any());
    }

    @Test
    void consultarUnaRecepcionInexistenteDevuelveNoEncontrada() {
        when(consultas.buscar(99)).thenReturn(Optional.empty());

        var error = assertThrows(RecepcionFailure.class, () ->
            new ObtenerRecepcionHandler(consultas).handle(new ObtenerRecepcionQuery(99)));

        assertEquals(RecepcionFailure.Reason.RECEPCION_NO_ENCONTRADA,error.reason());
    }

    private void prepararRegistro(long id) {
        when(proveedores.existeActivo(7)).thenReturn(true);
        when(kardex.maizId()).thenReturn(Optional.of(4L));
        when(recepciones.agregar(any())).thenReturn(id);
    }

    private Recepcion agregada() {
        var captor = ArgumentCaptor.forClass(Recepcion.class);
        verify(recepciones).agregar(captor.capture());
        return captor.getValue();
    }

    private DecidirRecepcionCommand decision(DecisionRecepcion decision, String motivo) {
        return new DecidirRecepcionCommand(11,decision,motivo,jefe);
    }

    private static RegistrarRecepcionCommand registro(String humedad, Actor actor) {
        return new RegistrarRecepcionCommand(7,new BigDecimal("1000"),new BigDecimal("100"),
            new BigDecimal("10"),new BigDecimal(humedad),new BigDecimal("10"),actor);
    }

    private static CorregirRecepcionCommand correccion(String humedad, Actor actor) {
        return new CorregirRecepcionCommand(11,7,new BigDecimal("1000"),new BigDecimal("100"),
            new BigDecimal("10"),new BigDecimal(humedad),new BigDecimal("10"),actor);
    }

    /** Recepción ya guardada: neto 890 kg y merma 10 kg, es decir 880 kg al kardex. */
    private static Recepcion guardada(EstadoRecepcion estado, Long idAutoriza) {
        return Recepcion.existente(11,7,4,3,
            PesajeMaiz.de(new BigDecimal("1000"),new BigDecimal("100"),new BigDecimal("10"),new BigDecimal("10")),
            PorcentajeHumedad.de(new BigDecimal("14.8")),estado,idAutoriza);
    }

    private static RecepcionResponse respuesta(EstadoRecepcion estado) {
        return new RecepcionResponse(11,LocalDateTime.of(2026,10,27,12,0),7,"Proveedor",
            new BigDecimal("1000"),new BigDecimal("100"),new BigDecimal("10"),
            new BigDecimal("890.00"),new BigDecimal("14.5"),new BigDecimal("10"),
            estado,null,null,null);
    }
}
