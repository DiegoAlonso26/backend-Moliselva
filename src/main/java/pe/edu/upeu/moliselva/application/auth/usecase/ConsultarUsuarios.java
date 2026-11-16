package pe.edu.upeu.moliselva.application.auth.usecase;

import java.util.List;
import pe.edu.upeu.moliselva.application.auth.gateway.UsuarioGateway;
import pe.edu.upeu.moliselva.application.shared.FallaAplicacion;
import pe.edu.upeu.moliselva.application.shared.FallaNegocio;
import pe.edu.upeu.moliselva.domain.auth.CuentaUsuario;
import pe.edu.upeu.moliselva.domain.auth.Usuario;

public class ConsultarUsuarios implements ConsultarUsuariosUseCase {
    private final UsuarioGateway usuarios;

    public ConsultarUsuarios(UsuarioGateway usuarios) { this.usuarios=usuarios; }

    @Override
    public Usuario actual(long id) {
        return usuarios.buscarPorId(id).filter(CuentaUsuario::activo).map(CuentaUsuario::publico)
            .orElseThrow(() -> new FallaAplicacion(FallaNegocio.Tipo.NO_AUTENTICADO,"NO_AUTENTICADO",
                "Usuario inactivo o inexistente"));
    }

    @Override
    public List<Usuario> listar() { return usuarios.listar(); }

    @Override
    public Usuario obtener(long id) {
        return usuarios.buscarPorId(id).map(CuentaUsuario::publico).orElseThrow(UsuarioFallas::noEncontrado);
    }
}
