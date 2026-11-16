package pe.edu.upeu.moliselva.application.maestros.usecase;

import pe.edu.upeu.moliselva.domain.maestros.Cliente;
import pe.edu.upeu.moliselva.domain.maestros.ClienteDatos;

/** ADMINISTRADOR o JEFE_PLANTA (se exige en la presentación). */
public interface GestionarClientesUseCase {
    Cliente crear(ClienteDatos datos, long usuarioId);
    Cliente actualizar(long id, ClienteDatos datos, long usuarioId);
    void desactivar(long id, long usuarioId);
}
