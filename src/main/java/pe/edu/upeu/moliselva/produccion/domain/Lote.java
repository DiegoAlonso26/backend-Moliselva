package pe.edu.upeu.moliselva.produccion.domain;

import java.time.LocalDateTime;

public record Lote(long idLote, long idProducto, long idFormula, LocalDateTime fechaHora,
    int tamanoKg, EstadoLote estado, long idResponsable) {}
