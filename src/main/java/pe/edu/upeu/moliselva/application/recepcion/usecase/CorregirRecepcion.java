package pe.edu.upeu.moliselva.application.recepcion.usecase;

import pe.edu.upeu.moliselva.application.shared.Actor;

import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.EntradaKardexGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.ProveedorActivoGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.RecepcionGateway;
import pe.edu.upeu.moliselva.domain.recepcion.EstadoRecepcion;
import pe.edu.upeu.moliselva.domain.recepcion.Recepcion;
import pe.edu.upeu.moliselva.domain.recepcion.RecepcionDatos;

public class CorregirRecepcion implements CorregirRecepcionUseCase {
    private final RecepcionGateway recepciones;
    private final ProveedorActivoGateway proveedores;
    private final EntradaKardexGateway inventario;
    private final BitacoraGateway bitacora;
    public CorregirRecepcion(RecepcionGateway recepciones, ProveedorActivoGateway proveedores,
            EntradaKardexGateway inventario, BitacoraGateway bitacora) {
        this.recepciones=recepciones; this.proveedores=proveedores;
        this.inventario=inventario; this.bitacora=bitacora;
    }

    @Override
    public Recepcion corregir(long id, RecepcionDatos datos, Actor actor) {
        RecepcionChecks.puedeRegistrar(actor);
        var locked=recepciones.bloquear(id).orElseThrow(RecepcionChecks::missing);
        RecepcionChecks.editable(locked);
        RecepcionChecks.proveedorActivo(proveedores,datos.idProveedor());
        var evaluacion=RecepcionChecks.evaluar(datos);
        if (!recepciones.corregir(id,datos,evaluacion.estado())) {
            throw RecepcionChecks.notEditable();
        }
        if (evaluacion.estado()==EstadoRecepcion.ACEPTADA) {
            inventario.registrarEntrada(locked.idInsumo(),id,evaluacion.entradaKardexKg(),actor.id());
        }
        bitacora.registrar(actor.id(),"ACTUALIZAR_RECEPCION id="+id+" estado="+evaluacion.estado(),null);
        return recepciones.buscar(id).orElseThrow(RecepcionChecks::missing);
    }
}
