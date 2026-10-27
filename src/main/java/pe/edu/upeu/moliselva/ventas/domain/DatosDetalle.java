package pe.edu.upeu.moliselva.ventas.domain;

import java.math.BigDecimal;

public record DatosDetalle(long idProducto, int cantidadSacos, BigDecimal cantidadKg) {}
