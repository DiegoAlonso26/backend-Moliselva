package pe.edu.upeu.moliselva.domain.formulas;

import java.time.LocalDateTime;

/** Fórmula de un producto. Solo la consulta el JEFE_PLANTA. */
public record Formula(Long idFormula, Long idProducto, String version, Boolean vigente,
        Long idCreador, LocalDateTime fechaCreacion) {}
