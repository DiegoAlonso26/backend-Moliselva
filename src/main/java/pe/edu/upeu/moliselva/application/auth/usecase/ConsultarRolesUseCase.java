package pe.edu.upeu.moliselva.application.auth.usecase;

import java.util.List;
import pe.edu.upeu.moliselva.domain.auth.Rol;

/** Roles configurados. Solo ADMINISTRADOR (se exige en la presentación). */
public interface ConsultarRolesUseCase {
    List<Rol> listar();
    Rol obtener(short id);
}
