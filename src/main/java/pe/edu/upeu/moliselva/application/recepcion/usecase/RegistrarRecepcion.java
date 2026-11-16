package pe.edu.upeu.moliselva.application.recepcion.usecase;

import pe.edu.upeu.moliselva.application.shared.Actor;

import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.EntradaKardexGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.ProveedorActivoGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.RecepcionGateway;
import pe.edu.upeu.moliselva.domain.recepcion.EstadoRecepcion;
import pe.edu.upeu.moliselva.domain.recepcion.Recepcion;
import pe.edu.upeu.moliselva.domain.recepcion.RecepcionDatos;

public class RegistrarRecepcion implements RegistrarRecepcionUseCase {
    private final RecepcionGateway recepciones;
    private final ProveedorActivoGateway proveedores;
    private final EntradaKardexGateway inventario;
    private final BitacoraGateway bitacora;

    public RegistrarRecepcion(RecepcionGateway recepciones, ProveedorActivoGateway proveedores,
            EntradaKardexGateway inventario, BitacoraGateway bitacora) {
        this.recepciones=recepciones; this.proveedores=proveedores;
        this.inventario=inventario; this.bitacora=bitacora;
    }

    @Override
    public Recepcion registrar(RecepcionDatos datos, Actor actor) {
        RecepcionChecks.puedeRegistrar(actor);
        RecepcionChecks.proveedorActivo(proveedores,datos.idProveedor());
        long idMaiz=RecepcionChecks.maizId(inventario);
        var evaluacion=RecepcionChecks.evaluar(datos);
        long id=recepciones.insertar(datos,idMaiz,evaluacion.estado(),actor.id());
        if (evaluacion.estado()==EstadoRecepcion.ACEPTADA) {
            inventario.registrarEntrada(idMaiz,id,evaluacion.entradaKardexKg(),actor.id());
        }
        bitacora.registrar(actor.id(),"REGISTRAR_RECEPCION id="+id+" estado="+evaluacion.estado(),null);
        return recepciones.buscar(id).orElseThrow(RecepcionChecks::missing)
            .conRegistradoPor(actor.usuario());
    }
}
