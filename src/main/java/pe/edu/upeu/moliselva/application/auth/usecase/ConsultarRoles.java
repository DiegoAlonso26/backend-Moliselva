package pe.edu.upeu.moliselva.application.auth.usecase;

import java.util.List;
import pe.edu.upeu.moliselva.application.auth.gateway.RolGateway;
import pe.edu.upeu.moliselva.application.shared.FallaAplicacion;
import pe.edu.upeu.moliselva.domain.auth.Rol;

public class ConsultarRoles implements ConsultarRolesUseCase {
    private final RolGateway roles;

    public ConsultarRoles(RolGateway roles) { this.roles=roles; }

    @Override
    public List<Rol> listar() { return roles.listar(); }

    @Override
    public Rol obtener(short id) {
        return roles.buscar(id).orElseThrow(() -> FallaAplicacion.noEncontrado("ROL_NO_ENCONTRADO","El rol no existe"));
    }
}
