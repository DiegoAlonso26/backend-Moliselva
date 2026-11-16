package pe.edu.upeu.moliselva.application.inventario.usecase;

import pe.edu.upeu.moliselva.domain.inventario.AjusteInventario;
import pe.edu.upeu.moliselva.domain.inventario.MovimientoInventario;

/** El kardex es inmutable: se corrige con un ajuste. ADMINISTRADOR, JEFE_PLANTA o CONTADORA. */
public interface RegistrarAjusteUseCase {
    MovimientoInventario ajustar(AjusteInventario ajuste, long usuarioId);
}
