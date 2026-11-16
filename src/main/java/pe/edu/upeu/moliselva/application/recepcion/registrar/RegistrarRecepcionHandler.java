package pe.edu.upeu.moliselva.application.recepcion.registrar;

import pe.edu.upeu.moliselva.application.recepcion.RecepcionResponse;
import pe.edu.upeu.moliselva.application.recepcion.ReglasRecepcion;
import pe.edu.upeu.moliselva.application.recepcion.gateway.EntradaKardexGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.ProveedorActivoGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.RecepcionConsultaGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.RecepcionGateway;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.shared.cqrs.CommandHandler;
import pe.edu.upeu.moliselva.domain.recepcion.PesajeMaiz;
import pe.edu.upeu.moliselva.domain.recepcion.PorcentajeHumedad;
import pe.edu.upeu.moliselva.domain.recepcion.Recepcion;

/**
 * Registra la recepción; si la humedad la deja ACEPTADA, ingresa el maíz al kardex.
 * Recepción, kardex y bitácora quedan en una sola transacción: la del command.
 */
public class RegistrarRecepcionHandler implements CommandHandler<RegistrarRecepcionCommand, RecepcionResponse> {
    private final RecepcionGateway recepciones;
    private final RecepcionConsultaGateway consultas;
    private final ProveedorActivoGateway proveedores;
    private final EntradaKardexGateway kardex;
    private final BitacoraGateway bitacora;

    public RegistrarRecepcionHandler(RecepcionGateway recepciones, RecepcionConsultaGateway consultas,
            ProveedorActivoGateway proveedores, EntradaKardexGateway kardex, BitacoraGateway bitacora) {
        this.recepciones=recepciones; this.consultas=consultas; this.proveedores=proveedores;
        this.kardex=kardex; this.bitacora=bitacora;
    }

    @Override
    public RecepcionResponse handle(RegistrarRecepcionCommand command) {
        var actor=command.actor();
        ReglasRecepcion.puedeRegistrar(actor);
        ReglasRecepcion.proveedorActivo(proveedores,command.idProveedor());
        long idMaiz=ReglasRecepcion.maizId(kardex);
        Recepcion recepcion=ReglasRecepcion.dominio(() -> Recepcion.registrar(command.idProveedor(),idMaiz,actor.id(),
            PesajeMaiz.de(command.pesoBrutoKg(),command.taraKg(),command.pesoSacosKg(),command.mermaLimpiezaKg()),
            PorcentajeHumedad.de(command.humedadPct())));

        long id=recepciones.agregar(recepcion);
        if (recepcion.ingresaAlKardex()) {
            kardex.registrarEntrada(idMaiz,id,recepcion.entradaKardex(),actor.id());
        }
        bitacora.registrar(actor.id(),"REGISTRAR_RECEPCION id="+id+" estado="+recepcion.estado(),null);
        return consultas.buscar(id).orElseThrow(ReglasRecepcion::noEncontrada).conRegistradoPor(actor.usuario());
    }
}
