package pe.edu.upeu.moliselva.application.auth.usecase;

import java.util.List;
import pe.edu.upeu.moliselva.domain.auth.Usuario;

public interface ConsultarUsuariosUseCase {
    /** Usuario de la sesión; si ya no está activo responde NO_AUTENTICADO. */
    Usuario actual(long id);
    List<Usuario> listar();
    Usuario obtener(long id);
}
