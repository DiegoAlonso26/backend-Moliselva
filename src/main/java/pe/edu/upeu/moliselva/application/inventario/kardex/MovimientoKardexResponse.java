package pe.edu.upeu.moliselva.application.inventario.kardex;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Línea del kardex de maíz con su saldo acumulado. Es un modelo de lectura: no pasa por el dominio. */
public record MovimientoKardexResponse(long idMovimiento, LocalDateTime fechaHora, String tipoMovimiento,
    BigDecimal cantidadKg, BigDecimal saldoKg, Long idRecepcion, String usuario) {}
