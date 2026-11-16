package pe.edu.upeu.moliselva.domain.auth;

/** Usuario con el hash de su clave. Solo lo usan el inicio de sesión y la validación del token. */
public record CuentaUsuario(long id, String nombres, String usuario, String hash, String rol, boolean activo) {
    public Usuario publico() {
        return new Usuario(id, nombres, usuario, rol, activo);
    }
}
