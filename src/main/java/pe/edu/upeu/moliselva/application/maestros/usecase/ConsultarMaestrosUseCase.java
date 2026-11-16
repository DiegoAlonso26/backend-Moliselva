package pe.edu.upeu.moliselva.application.maestros.usecase;

import java.util.List;
import pe.edu.upeu.moliselva.domain.maestros.Cliente;
import pe.edu.upeu.moliselva.domain.maestros.Insumo;
import pe.edu.upeu.moliselva.domain.maestros.Producto;

/** Clientes, insumos y productos activos. Los permisos de lectura se exigen en la presentación. */
public interface ConsultarMaestrosUseCase {
    List<Cliente> clientes();
    Cliente cliente(long id);
    List<Insumo> insumos();
    Insumo insumo(long id);
    List<Producto> productos();
    Producto producto(long id);
}
