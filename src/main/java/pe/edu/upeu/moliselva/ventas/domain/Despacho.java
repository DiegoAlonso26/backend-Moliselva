package pe.edu.upeu.moliselva.ventas.domain;

import java.time.LocalDateTime;

public record Despacho(long idDespacho, long idPedido, LocalDateTime fechaHora,
    TipoEntrega tipoEntrega, String destino, EstadoDespacho estado, long idUsuario) {}
