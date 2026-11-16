package pe.edu.upeu.moliselva.application.maestros.gateway;

import java.util.List;
import java.util.Optional;
import pe.edu.upeu.moliselva.domain.maestros.Cliente;
import pe.edu.upeu.moliselva.domain.maestros.ClienteDatos;

public interface ClienteGateway {
    List<Cliente> activos();
    Optional<Cliente> buscarActivo(long id);
    Cliente crear(ClienteDatos datos);
    Optional<Cliente> actualizar(long id, ClienteDatos datos);
    boolean desactivar(long id);
    boolean tienePedidosAbiertos(long id);
}
