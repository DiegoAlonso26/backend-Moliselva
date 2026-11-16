package pe.edu.upeu.moliselva.application.auth.usecase;

import pe.edu.upeu.moliselva.domain.auth.Usuario;
import pe.edu.upeu.moliselva.domain.auth.UsuarioDatos;

/** Alta, modificación y baja lógica de usuarios. Solo ADMINISTRADOR (se exige en la presentación). */
public interface GestionarUsuariosUseCase {
    Usuario crear(UsuarioDatos datos, String clave, long adminId);
    /** {@code clave} null conserva la clave actual. */
    Usuario actualizar(long id, UsuarioDatos datos, String clave, long adminId);
    void desactivar(long id, long adminId);
}
