package pe.edu.upeu.moliselva.application.produccion.gateway;

import java.math.BigDecimal;

public interface InventarioProduccionGateway {
    boolean tieneMovimientos(long idLote);
    void registrarSalida(long idLote, long idInsumo, BigDecimal cantidad, long usuarioId);
    void registrarEntrada(long idLote, long idProducto, int tamanoKg, long usuarioId);
}
