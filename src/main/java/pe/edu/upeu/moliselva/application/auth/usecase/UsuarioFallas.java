package pe.edu.upeu.moliselva.application.auth.usecase;

import pe.edu.upeu.moliselva.application.shared.FallaAplicacion;

final class UsuarioFallas {
    private UsuarioFallas() {}

    static FallaAplicacion noEncontrado() {
        return FallaAplicacion.noEncontrado("USUARIO_NO_ENCONTRADO","El usuario no existe o está inactivo");
    }

    static FallaAplicacion rolInvalido() {
        return FallaAplicacion.datosInvalidos("ROL_INVALIDO","Rol no reconocido");
    }

    static FallaAplicacion ultimoAdministrador(String mensaje) {
        return FallaAplicacion.conflicto("ULTIMO_ADMINISTRADOR",mensaje);
    }
}
