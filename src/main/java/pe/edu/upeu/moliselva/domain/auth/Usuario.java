package pe.edu.upeu.moliselva.domain.auth;

/** Usuario visible del sistema (sin su clave). */
public record Usuario(long id, String nombres, String usuario, String rol, boolean activo) {}
