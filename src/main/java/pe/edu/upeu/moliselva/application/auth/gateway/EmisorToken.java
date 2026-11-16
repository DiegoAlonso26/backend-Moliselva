package pe.edu.upeu.moliselva.application.auth.gateway;

import pe.edu.upeu.moliselva.domain.auth.CuentaUsuario;

/** Emite el token de sesión (JWT en infraestructura). */
public interface EmisorToken {
    String emitir(CuentaUsuario cuenta);
}
