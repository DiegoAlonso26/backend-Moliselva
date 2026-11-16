package pe.edu.upeu.moliselva.domain.produccion;

import java.time.LocalDateTime;

public record Lote(long idLote, long idProducto, long idFormula, LocalDateTime fechaHora,
    int tamanoKg, EstadoLote estado, long idResponsable) {}
