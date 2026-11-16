package pe.edu.upeu.moliselva.domain.ventas;

import java.math.BigDecimal;

public record DatosDetalle(long idProducto, int cantidadSacos, BigDecimal cantidadKg) {}
