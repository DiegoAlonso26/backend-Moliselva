package pe.edu.upeu.moliselva.presentation.rest.recepcion.mapper;

import pe.edu.upeu.moliselva.domain.recepcion.DecisionRecepcion;
import pe.edu.upeu.moliselva.domain.recepcion.Recepcion;
import pe.edu.upeu.moliselva.domain.recepcion.RecepcionDatos;
import pe.edu.upeu.moliselva.presentation.rest.recepcion.dto.RecepcionDto;

/** Convierte entre el JSON de la API y los datos que usan los casos de uso de recepción. */
public final class RecepcionMapper {
    private RecepcionMapper() {}

    public static RecepcionDatos aDatos(RecepcionDto.Crear input) {
        return new RecepcionDatos(input.idProveedor(),input.pesoBrutoKg(),input.taraKg(),
            input.pesoSacosKg(),input.humedadPct(),input.mermaLimpiezaKg());
    }

    public static DecisionRecepcion aDecision(RecepcionDto.Decision input) {
        return DecisionRecepcion.valueOf(input.decision());
    }

    public static RecepcionDto.Respuesta aRespuesta(Recepcion recepcion) {
        return new RecepcionDto.Respuesta(recepcion.idRecepcion(),recepcion.fechaHora(),
            recepcion.idProveedor(),recepcion.proveedor(),recepcion.pesoBrutoKg(),
            recepcion.taraKg(),recepcion.pesoSacosKg(),recepcion.pesoNetoKg(),
            recepcion.humedadPct(),recepcion.mermaLimpiezaKg(),recepcion.estado().name(),
            recepcion.entradaKardexKg(),recepcion.registradoPor(),recepcion.autorizadoPor());
    }
}
