package pe.edu.upeu.moliselva.ventas.domain;

import java.math.BigDecimal;

public record PedidoDetalle(long idPedido, long idProducto, int cantidadSacos,
    BigDecimal cantidadKg) {}
