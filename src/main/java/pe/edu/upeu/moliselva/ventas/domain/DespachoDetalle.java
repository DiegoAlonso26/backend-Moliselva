package pe.edu.upeu.moliselva.ventas.domain;

import java.math.BigDecimal;

public record DespachoDetalle(long idDespacho, long idProducto, int cantidadSacos,
    BigDecimal cantidadKg) {}
