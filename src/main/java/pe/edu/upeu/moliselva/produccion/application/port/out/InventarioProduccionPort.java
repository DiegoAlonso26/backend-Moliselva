package pe.edu.upeu.moliselva.produccion.application.port.out;

import java.math.BigDecimal;

public interface InventarioProduccionPort {
    boolean tieneMovimientos(long idLote);
    void registrarSalida(long idLote, long idInsumo, BigDecimal cantidad, long usuarioId);
    void registrarEntrada(long idLote, long idProducto, int tamanoKg, long usuarioId);
}
