package pe.edu.upeu.moliselva.recepcion.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.moliselva.recepcion.application.port.in.EliminarRecepcionUseCase;
import pe.edu.upeu.moliselva.recepcion.application.port.out.BitacoraPort;
import pe.edu.upeu.moliselva.recepcion.application.port.out.RecepcionPort;

@Service
public class EliminarRecepcion implements EliminarRecepcionUseCase {
    private final RecepcionPort recepciones;
    private final BitacoraPort bitacora;
    public EliminarRecepcion(RecepcionPort recepciones, BitacoraPort bitacora) {
        this.recepciones=recepciones; this.bitacora=bitacora;
    }

    @Override @Transactional
    public void eliminar(long id, ActorRecepcion actor) {
        RecepcionChecks.puedeRegistrar(actor);
        var locked=recepciones.bloquear(id).orElseThrow(RecepcionChecks::missing);
        RecepcionChecks.editable(locked);
        if (!recepciones.eliminarEditable(id)) { throw RecepcionChecks.notEditable(); }
        bitacora.registrar(actor.id(),"ELIMINAR_RECEPCION id="+id,null);
    }
}
