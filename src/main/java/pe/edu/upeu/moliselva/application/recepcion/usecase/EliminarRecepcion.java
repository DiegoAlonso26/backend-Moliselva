package pe.edu.upeu.moliselva.application.recepcion.usecase;

import pe.edu.upeu.moliselva.application.shared.Actor;

import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.RecepcionGateway;

public class EliminarRecepcion implements EliminarRecepcionUseCase {
    private final RecepcionGateway recepciones;
    private final BitacoraGateway bitacora;
    public EliminarRecepcion(RecepcionGateway recepciones, BitacoraGateway bitacora) {
        this.recepciones=recepciones; this.bitacora=bitacora;
    }

    @Override
    public void eliminar(long id, Actor actor) {
        RecepcionChecks.puedeRegistrar(actor);
        var locked=recepciones.bloquear(id).orElseThrow(RecepcionChecks::missing);
        RecepcionChecks.editable(locked);
        if (!recepciones.eliminarEditable(id)) { throw RecepcionChecks.notEditable(); }
        bitacora.registrar(actor.id(),"ELIMINAR_RECEPCION id="+id,null);
    }
}
