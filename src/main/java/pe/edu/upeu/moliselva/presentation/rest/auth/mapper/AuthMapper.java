package pe.edu.upeu.moliselva.presentation.rest.auth.mapper;

import pe.edu.upeu.moliselva.application.auth.usecase.Sesion;
import pe.edu.upeu.moliselva.domain.auth.Usuario;
import pe.edu.upeu.moliselva.domain.auth.UsuarioDatos;
import pe.edu.upeu.moliselva.presentation.rest.auth.dto.AuthDto;
import pe.edu.upeu.moliselva.presentation.rest.auth.dto.Rol;

public final class AuthMapper {
    private AuthMapper() {}

    public static AuthDto.Usuario aRespuesta(Usuario u) {
        return new AuthDto.Usuario(u.id(),u.nombres(),u.usuario(),u.rol(),u.activo());
    }

    public static AuthDto.LoginResponse aRespuesta(Sesion sesion) {
        return new AuthDto.LoginResponse(sesion.token(),aRespuesta(sesion.usuario()));
    }

    public static UsuarioDatos aDatos(AuthDto.NuevoUsuario input) {
        return new UsuarioDatos(input.nombres(),input.usuario(),input.rol());
    }

    public static UsuarioDatos aDatos(AuthDto.ActualizarUsuario input) {
        return new UsuarioDatos(input.nombres(),input.usuario(),input.rol());
    }

    public static Rol aRespuesta(pe.edu.upeu.moliselva.domain.auth.Rol rol) {
        return new Rol(rol.idRol(),rol.nombre(),rol.descripcion());
    }
}
