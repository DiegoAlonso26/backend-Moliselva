package pe.edu.upeu.moliselva.application.auth.gateway;

import java.util.List;
import java.util.Optional;
import pe.edu.upeu.moliselva.domain.auth.Rol;

public interface RolGateway {
    List<Rol> listar();
    Optional<Rol> buscar(short id);
}
