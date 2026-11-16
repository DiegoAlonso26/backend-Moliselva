package pe.edu.upeu.moliselva.domain.ventas;

import java.math.BigDecimal;

public record DespachoDetalle(long idDespacho, long idProducto, int cantidadSacos,
    BigDecimal cantidadKg) {}
