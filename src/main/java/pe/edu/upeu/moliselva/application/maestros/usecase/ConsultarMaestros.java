package pe.edu.upeu.moliselva.application.maestros.usecase;

import java.util.List;
import pe.edu.upeu.moliselva.application.maestros.gateway.ClienteGateway;
import pe.edu.upeu.moliselva.application.maestros.gateway.InsumoGateway;
import pe.edu.upeu.moliselva.application.maestros.gateway.ProductoGateway;
import pe.edu.upeu.moliselva.domain.maestros.Cliente;
import pe.edu.upeu.moliselva.domain.maestros.Insumo;
import pe.edu.upeu.moliselva.domain.maestros.Producto;

public class ConsultarMaestros implements ConsultarMaestrosUseCase {
    private final ClienteGateway clientes;
    private final InsumoGateway insumos;
    private final ProductoGateway productos;

    public ConsultarMaestros(ClienteGateway clientes, InsumoGateway insumos, ProductoGateway productos) {
        this.clientes=clientes; this.insumos=insumos; this.productos=productos;
    }

    @Override public List<Cliente> clientes() { return clientes.activos(); }
    @Override public Cliente cliente(long id) {
        return clientes.buscarActivo(id).orElseThrow(() -> MaestrosFallas.noEncontrado("CLIENTE"));
    }
    @Override public List<Insumo> insumos() { return insumos.activos(); }
    @Override public Insumo insumo(long id) {
        return insumos.buscarActivo(id).orElseThrow(() -> MaestrosFallas.noEncontrado("INSUMO"));
    }
    @Override public List<Producto> productos() { return productos.activos(); }
    @Override public Producto producto(long id) {
        return productos.buscarActivo(id).orElseThrow(() -> MaestrosFallas.noEncontrado("PRODUCTO"));
    }
}
