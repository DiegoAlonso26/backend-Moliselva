package pe.edu.upeu.moliselva.application.auth.gateway;

import java.util.List;
import java.util.Optional;
import pe.edu.upeu.moliselva.domain.auth.CuentaUsuario;
import pe.edu.upeu.moliselva.domain.auth.Usuario;
import pe.edu.upeu.moliselva.domain.auth.UsuarioDatos;

public interface UsuarioGateway {
    Optional<CuentaUsuario> buscarPorUsuario(String usuario);
    Optional<CuentaUsuario> buscarPorId(long id);
    List<Usuario> listar();
    boolean existeRol(String rol);
    long crear(UsuarioDatos datos, String hash);
    /** {@code hash} null conserva la clave actual. */
    boolean actualizar(long id, UsuarioDatos datos, String hash);
    boolean desactivar(long id);
    int contarClavesDemoPendientes();
    void asignarClaveDemo(String hash);
}
