package pe.edu.upeu.moliselva.application.recepcion.gateway;

import java.util.Optional;
import pe.edu.upeu.moliselva.domain.shared.Kilogramos;

/** Entrada de maíz al kardex cuando una recepción queda aceptada. */
public interface EntradaKardexGateway {
    Optional<Long> maizId();

    void registrarEntrada(long idInsumo, long idRecepcion, Kilogramos cantidad, long idUsuario);
}
