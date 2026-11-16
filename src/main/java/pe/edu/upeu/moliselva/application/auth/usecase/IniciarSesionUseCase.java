package pe.edu.upeu.moliselva.application.auth.usecase;

public interface IniciarSesionUseCase {
    Sesion iniciar(String usuario, String clave);
}
