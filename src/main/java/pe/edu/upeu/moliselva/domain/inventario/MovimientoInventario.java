package pe.edu.upeu.moliselva.domain.inventario;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Movimiento del kardex general; es inmutable y se corrige con ajustes. */
public record MovimientoInventario(long idMovimiento, LocalDateTime fechaHora, String tipoMovimiento,
    Long idInsumo, Long idProducto, BigDecimal cantidad, String unidadMedida,
    Long idRecepcion, Long idLote, Long idDespacho, String usuario) {}
