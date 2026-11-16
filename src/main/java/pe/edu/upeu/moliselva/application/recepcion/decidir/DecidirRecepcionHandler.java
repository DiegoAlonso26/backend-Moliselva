package pe.edu.upeu.moliselva.application.recepcion.decidir;

import pe.edu.upeu.moliselva.application.recepcion.RecepcionResponse;
import pe.edu.upeu.moliselva.application.recepcion.ReglasRecepcion;
import pe.edu.upeu.moliselva.application.recepcion.gateway.EntradaKardexGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.RecepcionConsultaGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.RecepcionGateway;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.shared.cqrs.CommandHandler;
import pe.edu.upeu.moliselva.domain.shared.Motivo;

/** Decide una recepción pendiente; si la acepta, el maíz ingresa al kardex una sola vez. */
public class DecidirRecepcionHandler implements CommandHandler<DecidirRecepcionCommand, RecepcionResponse> {
    private final RecepcionGateway recepciones;
    private final RecepcionConsultaGateway consultas;
    private final EntradaKardexGateway kardex;
    private final BitacoraGateway bitacora;

    public DecidirRecepcionHandler(RecepcionGateway recepciones, RecepcionConsultaGateway consultas,
            EntradaKardexGateway kardex, BitacoraGateway bitacora) {
        this.recepciones=recepciones; this.consultas=consultas; this.kardex=kardex; this.bitacora=bitacora;
    }

    @Override
    public RecepcionResponse handle(DecidirRecepcionCommand command) {
        var actor=command.actor();
        ReglasRecepcion.puedeDecidir(actor);
        var recepcion=recepciones.bloquear(command.idRecepcion()).orElseThrow(ReglasRecepcion::noEncontrada);
        ReglasRecepcion.dominio(() -> recepcion.decidir(command.decision(),actor.id()));

        recepciones.guardar(recepcion);
        long id=recepcion.id();
        if (recepcion.ingresaAlKardex()) {
            kardex.registrarEntrada(recepcion.idInsumo(),id,recepcion.entradaKardex(),actor.id());
        }
        bitacora.registrar(actor.id(),"DECISION_RECEPCION id="+id+" estado="+recepcion.estado(),
            Motivo.de(command.motivo()).texto());
        return consultas.buscar(id).orElseThrow(ReglasRecepcion::noEncontrada);
    }
}
