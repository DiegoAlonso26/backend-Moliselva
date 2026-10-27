package pe.edu.upeu.moliselva.ventas.domain;

import java.time.LocalDateTime;

public record Pedido(long idPedido, long idCliente, LocalDateTime fechaHora,
    EstadoPedido estado, String observacion) {}
