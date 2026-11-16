package pe.edu.upeu.moliselva.application.maestros.usecase;

import pe.edu.upeu.moliselva.application.maestros.gateway.ClienteGateway;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.shared.FallaAplicacion;
import pe.edu.upeu.moliselva.domain.maestros.Cliente;
import pe.edu.upeu.moliselva.domain.maestros.ClienteDatos;

public class GestionarClientes implements GestionarClientesUseCase {
    private final ClienteGateway clientes;
    private final BitacoraGateway bitacora;

    public GestionarClientes(ClienteGateway clientes, BitacoraGateway bitacora) {
        this.clientes=clientes; this.bitacora=bitacora;
    }

    @Override
    public Cliente crear(ClienteDatos datos, long usuarioId) {
        var cliente=clientes.crear(datos);
        bitacora.registrar(usuarioId,"CREAR_CLIENTE id="+cliente.idCliente(),null);
        return cliente;
    }

    @Override
    public Cliente actualizar(long id, ClienteDatos datos, long usuarioId) {
        var cliente=clientes.actualizar(id,datos).orElseThrow(() -> MaestrosFallas.noEncontrado("CLIENTE"));
        bitacora.registrar(usuarioId,"ACTUALIZAR_CLIENTE id="+id,null);
        return cliente;
    }

    @Override
    public void desactivar(long id, long usuarioId) {
        if (clientes.tienePedidosAbiertos(id)) {
            throw FallaAplicacion.conflicto("CLIENTE_CON_PEDIDOS","El cliente tiene pedidos abiertos");
        }
        if (!clientes.desactivar(id)) { throw MaestrosFallas.noEncontrado("CLIENTE"); }
        bitacora.registrar(usuarioId,"DESACTIVAR_CLIENTE id="+id,null);
    }
}
