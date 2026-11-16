package pe.edu.upeu.moliselva.application.auth.usecase;

/**
 * Reemplaza el marcador HASH_DEMO_REEMPLAZAR de los cuatro usuarios de prueba por el
 * hash de DEMO_PASSWORD. No toca claves ya configuradas.
 */
public interface ActivarUsuariosDemoUseCase {
    void activar(String claveDemo);
}
