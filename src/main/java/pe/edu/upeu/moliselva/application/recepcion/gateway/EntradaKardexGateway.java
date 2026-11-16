package pe.edu.upeu.moliselva.application.recepcion.gateway;

import java.math.BigDecimal;
import java.util.Optional;

public interface EntradaKardexGateway {
    Optional<Long> maizId();
    void registrarEntrada(long idInsumo, long idRecepcion, BigDecimal cantidad, long idUsuario);
}
