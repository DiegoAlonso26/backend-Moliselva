package pe.edu.upeu.moliselva.recepcion.domain;

import java.math.BigDecimal;

public record RecepcionBloqueo(long idInsumo, BigDecimal pesoNetoKg,
    BigDecimal mermaLimpiezaKg, EstadoRecepcion estado, Long idAutoriza) {
    public boolean editable() {
        return estado != EstadoRecepcion.ACEPTADA && idAutoriza == null;
    }
}
