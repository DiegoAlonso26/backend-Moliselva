package pe.edu.upeu.moliselva.application.recepcion.usecase;

import pe.edu.upeu.moliselva.application.shared.Actor;
import pe.edu.upeu.moliselva.domain.recepcion.Recepcion;
import pe.edu.upeu.moliselva.domain.recepcion.RecepcionDatos;

public interface RegistrarRecepcionUseCase {
    Recepcion registrar(RecepcionDatos datos, Actor actor);
}
