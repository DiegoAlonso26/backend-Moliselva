package pe.edu.upeu.moliselva.domain.ventas;

import java.math.BigDecimal;

public record PedidoDetalle(long idPedido, long idProducto, int cantidadSacos,
    BigDecimal cantidadKg) {}
