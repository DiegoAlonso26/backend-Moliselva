package pe.edu.upeu.moliselva.application.recepcion.corregir;

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

/** Corrige la recepción; si la corrección la deja ACEPTADA, crea la entrada al kardex. */
public class CorregirRecepcionHandler implements CommandHandler<CorregirRecepcionCommand, RecepcionResponse> {
    private final RecepcionGateway recepciones;
    private final RecepcionConsultaGateway consultas;
    private final ProveedorActivoGateway proveedores;
    private final EntradaKardexGateway kardex;
    private final BitacoraGateway bitacora;

    public CorregirRecepcionHandler(RecepcionGateway recepciones, RecepcionConsultaGateway consultas,
            ProveedorActivoGateway proveedores, EntradaKardexGateway kardex, BitacoraGateway bitacora) {
        this.recepciones=recepciones; this.consultas=consultas; this.proveedores=proveedores;
        this.kardex=kardex; this.bitacora=bitacora;
    }

    @Override
    public RecepcionResponse handle(CorregirRecepcionCommand command) {
        var actor=command.actor();
        ReglasRecepcion.puedeRegistrar(actor);
        var recepcion=recepciones.bloquear(command.idRecepcion()).orElseThrow(ReglasRecepcion::noEncontrada);
        ReglasRecepcion.dominio(recepcion::verificarEditable);
        ReglasRecepcion.proveedorActivo(proveedores,command.idProveedor());
        ReglasRecepcion.dominio(() -> recepcion.corregir(command.idProveedor(),
            PesajeMaiz.de(command.pesoBrutoKg(),command.taraKg(),command.pesoSacosKg(),command.mermaLimpiezaKg()),
            PorcentajeHumedad.de(command.humedadPct())));

        recepciones.guardar(recepcion);
        long id=recepcion.id();
        if (recepcion.ingresaAlKardex()) {
            kardex.registrarEntrada(recepcion.idInsumo(),id,recepcion.entradaKardex(),actor.id());
        }
        bitacora.registrar(actor.id(),"ACTUALIZAR_RECEPCION id="+id+" estado="+recepcion.estado(),null);
        return consultas.buscar(id).orElseThrow(ReglasRecepcion::noEncontrada);
    }
}
