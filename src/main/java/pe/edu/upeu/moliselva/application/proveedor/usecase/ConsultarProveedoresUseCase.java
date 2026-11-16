package pe.edu.upeu.moliselva.application.proveedor.usecase;

import java.util.List;
import pe.edu.upeu.moliselva.domain.proveedor.Proveedor;

/** Consulta de proveedores activos. Disponible para los cuatro roles. */
public interface ConsultarProveedoresUseCase {
    List<Proveedor> listar();
    Proveedor obtener(long id);
}
