package pe.edu.upeu.moliselva.application.recepcion.registrar;

import java.math.BigDecimal;
import pe.edu.upeu.moliselva.application.recepcion.RecepcionResponse;
import pe.edu.upeu.moliselva.application.shared.Actor;
import pe.edu.upeu.moliselva.application.shared.cqrs.Command;

/** Registrar una recepción de maíz pesada en la balanza. Lo ejecuta un OPERARIO o el JEFE_PLANTA. */
public record RegistrarRecepcionCommand(long idProveedor, BigDecimal pesoBrutoKg, BigDecimal taraKg,
    BigDecimal pesoSacosKg, BigDecimal humedadPct, BigDecimal mermaLimpiezaKg, Actor actor)
    implements Command<RecepcionResponse> {}
