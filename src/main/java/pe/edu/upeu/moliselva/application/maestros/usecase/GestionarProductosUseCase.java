package pe.edu.upeu.moliselva.application.maestros.usecase;

import pe.edu.upeu.moliselva.domain.maestros.Producto;
import pe.edu.upeu.moliselva.domain.maestros.ProductoDatos;

/** ADMINISTRADOR o JEFE_PLANTA (se exige en la presentación). */
public interface GestionarProductosUseCase {
    Producto crear(ProductoDatos datos, long usuarioId);
    Producto actualizar(long id, ProductoDatos datos, long usuarioId);
    void desactivar(long id, long usuarioId);
}
