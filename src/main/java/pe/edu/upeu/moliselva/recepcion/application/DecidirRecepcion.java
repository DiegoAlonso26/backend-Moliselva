package pe.edu.upeu.moliselva.recepcion.application;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.moliselva.recepcion.application.port.in.DecidirRecepcionUseCase;
import pe.edu.upeu.moliselva.recepcion.application.port.out.BitacoraPort;
import pe.edu.upeu.moliselva.recepcion.application.port.out.InventarioPort;
import pe.edu.upeu.moliselva.recepcion.application.port.out.RecepcionPort;
import pe.edu.upeu.moliselva.recepcion.domain.DecisionRecepcion;
import pe.edu.upeu.moliselva.recepcion.domain.EstadoRecepcion;
import pe.edu.upeu.moliselva.recepcion.domain.Recepcion;
import pe.edu.upeu.moliselva.recepcion.domain.RecepcionPolicy;

@Service
public class DecidirRecepcion implements DecidirRecepcionUseCase {
    private final RecepcionPort recepciones;
    private final InventarioPort inventario;
    private final BitacoraPort bitacora;
    public DecidirRecepcion(RecepcionPort recepciones, InventarioPort inventario, BitacoraPort bitacora) {
        this.recepciones=recepciones; this.inventario=inventario; this.bitacora=bitacora;
    }

    @Override @Transactional
    public Recepcion decidir(long id, DecisionRecepcion decision, String motivo, ActorRecepcion actor) {
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
