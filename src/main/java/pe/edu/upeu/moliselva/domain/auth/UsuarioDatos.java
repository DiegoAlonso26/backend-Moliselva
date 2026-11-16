package pe.edu.upeu.moliselva.domain.auth;

/** Datos editables de un usuario: nombres y usuario se guardan sin espacios sobrantes. */
public record UsuarioDatos(String nombres, String usuario, String rol) {
    public UsuarioDatos {
        nombres = nombres.trim();
        usuario = usuario.trim();
    }
}
