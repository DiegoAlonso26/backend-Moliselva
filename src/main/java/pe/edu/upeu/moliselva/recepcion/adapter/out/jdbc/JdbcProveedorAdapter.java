package pe.edu.upeu.moliselva.recepcion.adapter.out.jdbc;

import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.proveedor.ProveedorRepository;
import pe.edu.upeu.moliselva.recepcion.application.port.out.ProveedorPort;

@Repository
public class JdbcProveedorAdapter implements ProveedorPort {
    private final ProveedorRepository proveedores;
    public JdbcProveedorAdapter(ProveedorRepository proveedores) { this.proveedores=proveedores; }
    @Override
    public boolean existeActivo(long idProveedor) { return proveedores.existeActivo(idProveedor); }
}
