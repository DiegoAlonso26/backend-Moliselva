package pe.edu.upeu.moliselva.application.maestros.usecase;

import pe.edu.upeu.moliselva.domain.maestros.Insumo;
import pe.edu.upeu.moliselva.domain.maestros.InsumoDatos;

/** ADMINISTRADOR o JEFE_PLANTA (se exige en la presentación). El insumo Maíz está protegido. */
public interface GestionarInsumosUseCase {
    Insumo crear(InsumoDatos datos, long usuarioId);
    Insumo actualizar(long id, InsumoDatos datos, long usuarioId);
    void desactivar(long id, long usuarioId);
}
