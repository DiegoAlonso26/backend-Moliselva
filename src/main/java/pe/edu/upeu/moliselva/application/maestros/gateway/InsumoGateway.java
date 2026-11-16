package pe.edu.upeu.moliselva.application.maestros.gateway;

import java.util.List;
import java.util.Optional;
import pe.edu.upeu.moliselva.domain.maestros.Insumo;
import pe.edu.upeu.moliselva.domain.maestros.InsumoDatos;

public interface InsumoGateway {
    List<Insumo> activos();
    Optional<Insumo> buscarActivo(long id);
    Insumo crear(InsumoDatos datos);
    Optional<Insumo> actualizar(long id, InsumoDatos datos);
    boolean desactivar(long id);
    /** Figura en una fórmula vigente o en un lote planificado o en proceso. */
    boolean enUso(long id);
}
