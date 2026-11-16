package pe.edu.upeu.moliselva.application.proveedor.usecase;

import pe.edu.upeu.moliselva.application.proveedor.gateway.ProveedorGateway;
import pe.edu.upeu.moliselva.application.shared.Actor;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.shared.Permisos;
import pe.edu.upeu.moliselva.domain.proveedor.Proveedor;
import pe.edu.upeu.moliselva.domain.proveedor.ProveedorDatos;

public class GestionarProveedor implements GestionarProveedorUseCase {
    private static final String SIN_PERMISO = "Solo el administrador o el jefe de planta gestionan proveedores";
    private final ProveedorGateway proveedores;
    private final BitacoraGateway bitacora;

    public GestionarProveedor(ProveedorGateway proveedores, BitacoraGateway bitacora) {
        this.proveedores=proveedores; this.bitacora=bitacora;
    }

    @Override
    public Proveedor crear(ProveedorDatos datos, Actor actor) {
        exigirPermiso(actor);
        Proveedor proveedor=proveedores.crear(datos);
        bitacora.registrar(actor.id(),"CREAR_PROVEEDOR id="+proveedor.idProveedor(),null);
        return proveedor;
    }

    @Override
    public Proveedor actualizar(long id, ProveedorDatos datos, Actor actor) {
        exigirPermiso(actor);
        Proveedor proveedor=proveedores.actualizar(id,datos).orElseThrow(ProveedorNoEncontrado::new);
        bitacora.registrar(actor.id(),"ACTUALIZAR_PROVEEDOR id="+id,null);
        return proveedor;
    }

    @Override
    public void desactivar(long id, Actor actor) {
        exigirPermiso(actor);
        if (!proveedores.desactivar(id)) { throw new ProveedorNoEncontrado(); }
        bitacora.registrar(actor.id(),"DESACTIVAR_PROVEEDOR id="+id,null);
    }

    private static void exigirPermiso(Actor actor) {
        Permisos.exigir(actor,SIN_PERMISO,Permisos.ADMINISTRADOR,Permisos.JEFE_PLANTA);
    }
}
