package pe.edu.upeu.moliselva.domain.inventario;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Línea del kardex de maíz con su saldo acumulado. */
public record MovimientoKardex(long idMovimiento, LocalDateTime fechaHora, String tipoMovimiento,
    BigDecimal cantidadKg, BigDecimal saldoKg, Long idRecepcion, String usuario) {}
