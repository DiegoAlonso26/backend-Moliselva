package pe.edu.upeu.moliselva.application.recepcion.usecase;

import pe.edu.upeu.moliselva.application.shared.Actor;

import java.math.BigDecimal;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.EntradaKardexGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.RecepcionGateway;
import pe.edu.upeu.moliselva.domain.recepcion.DecisionRecepcion;
import pe.edu.upeu.moliselva.domain.recepcion.EstadoRecepcion;
import pe.edu.upeu.moliselva.domain.recepcion.Recepcion;
import pe.edu.upeu.moliselva.domain.recepcion.RecepcionPolicy;

public class DecidirRecepcion implements DecidirRecepcionUseCase {
    private final RecepcionGateway recepciones;
    private final EntradaKardexGateway inventario;
    private final BitacoraGateway bitacora;
    public DecidirRecepcion(RecepcionGateway recepciones, EntradaKardexGateway inventario, BitacoraGateway bitacora) {
        this.recepciones=recepciones; this.inventario=inventario; this.bitacora=bitacora;
    }

    @Override
    public Recepcion decidir(long id, DecisionRecepcion decision, String motivo, Actor actor) {
        RecepcionChecks.puedeDecidir(actor);
        var locked=recepciones.bloquear(id).orElseThrow(RecepcionChecks::missing);
        if (locked.estado()!=EstadoRecepcion.PENDIENTE) {
            throw new RecepcionFailure(RecepcionFailure.Reason.RECEPCION_NO_PENDIENTE,
                "La recepción ya tiene una decisión");
        }
        EstadoRecepcion estado=decision==DecisionRecepcion.ACEPTAR
            ? EstadoRecepcion.ACEPTADA : EstadoRecepcion.RECHAZADA;
        if (!recepciones.decidir(id,estado,actor.id())) {
            throw new RecepcionFailure(RecepcionFailure.Reason.RECEPCION_NO_PENDIENTE,
                "La recepción ya tiene una decisión");
        }
        if (estado==EstadoRecepcion.ACEPTADA) {
            BigDecimal entrada;
            try {
                entrada=RecepcionPolicy.entradaKardex(locked.pesoNetoKg(),locked.mermaLimpiezaKg());
            } catch (IllegalArgumentException ex) {
                throw RecepcionChecks.invalid(ex);
            }
            inventario.registrarEntrada(locked.idInsumo(),id,entrada,actor.id());
        }
        bitacora.registrar(actor.id(),"DECISION_RECEPCION id="+id+" estado="+estado,motivo.trim());
        return recepciones.buscar(id).orElseThrow(RecepcionChecks::missing);
    }
}
