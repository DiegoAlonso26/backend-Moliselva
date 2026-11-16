package pe.edu.upeu.moliselva.application.proveedor.usecase;

import java.util.List;
import pe.edu.upeu.moliselva.application.proveedor.gateway.ProveedorGateway;
import pe.edu.upeu.moliselva.domain.proveedor.Proveedor;

public class ConsultarProveedores implements ConsultarProveedoresUseCase {
    private final ProveedorGateway proveedores;

    public ConsultarProveedores(ProveedorGateway proveedores) { this.proveedores=proveedores; }

    @Override
    public List<Proveedor> listar() { return proveedores.activos(); }

    @Override
    public Proveedor obtener(long id) {
        return proveedores.buscarActivo(id).orElseThrow(ProveedorNoEncontrado::new);
    }
}
