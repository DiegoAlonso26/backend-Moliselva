package pe.edu.upeu.moliselva.application.auth.gateway;

/** Hash de claves (BCrypt en infraestructura). */
public interface CifradorClaves {
    String cifrar(String clave);
    boolean coincide(String clave, String hash);
}
