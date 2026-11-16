package pe.edu.upeu.moliselva.application.recepcion.eliminar;

import pe.edu.upeu.moliselva.application.recepcion.ReglasRecepcion;
import pe.edu.upeu.moliselva.application.recepcion.gateway.RecepcionGateway;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.shared.cqrs.CommandHandler;

public class EliminarRecepcionHandler implements CommandHandler<EliminarRecepcionCommand, Void> {
    private final RecepcionGateway recepciones;
    private final BitacoraGateway bitacora;

    public EliminarRecepcionHandler(RecepcionGateway recepciones, BitacoraGateway bitacora) {
        this.recepciones=recepciones; this.bitacora=bitacora;
    }

    @Override
    public Void handle(EliminarRecepcionCommand command) {
        var actor=command.actor();
        ReglasRecepcion.puedeRegistrar(actor);
        var recepcion=recepciones.bloquear(command.idRecepcion()).orElseThrow(ReglasRecepcion::noEncontrada);
        ReglasRecepcion.dominio(recepcion::verificarEditable);
        recepciones.eliminar(recepcion);
        bitacora.registrar(actor.id(),"ELIMINAR_RECEPCION id="+recepcion.id(),null);
        return null;
    }
}
