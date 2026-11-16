package pe.edu.upeu.moliselva.application.recepcion.corregir;

import java.math.BigDecimal;
import pe.edu.upeu.moliselva.application.recepcion.RecepcionResponse;
import pe.edu.upeu.moliselva.application.shared.Actor;
import pe.edu.upeu.moliselva.application.shared.cqrs.Command;

/** Corregir una recepción PENDIENTE o RECHAZADA automáticamente, sin decisión del jefe. */
public record CorregirRecepcionCommand(long idRecepcion, long idProveedor, BigDecimal pesoBrutoKg,
    BigDecimal taraKg, BigDecimal pesoSacosKg, BigDecimal humedadPct, BigDecimal mermaLimpiezaKg, Actor actor)
    implements Command<RecepcionResponse> {}
