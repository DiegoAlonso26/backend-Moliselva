package pe.edu.upeu.moliselva.recepcion.application.port.out;

import java.math.BigDecimal;
import java.util.Optional;

public interface InventarioPort {
    Optional<Long> maizId();
    void registrarEntrada(long idInsumo, long idRecepcion, BigDecimal cantidad, long idUsuario);
}
