package pe.edu.upeu.moliselva.presentation.rest.recepcion.mapper;

import pe.edu.upeu.moliselva.application.recepcion.RecepcionResponse;
import pe.edu.upeu.moliselva.application.recepcion.corregir.CorregirRecepcionCommand;
import pe.edu.upeu.moliselva.application.recepcion.decidir.DecidirRecepcionCommand;
import pe.edu.upeu.moliselva.application.recepcion.registrar.RegistrarRecepcionCommand;
import pe.edu.upeu.moliselva.application.shared.Actor;
import pe.edu.upeu.moliselva.domain.recepcion.DecisionRecepcion;
import pe.edu.upeu.moliselva.presentation.rest.recepcion.dto.RecepcionDto;

/** Convierte el JSON de /api/recepciones en commands y la respuesta del caso de uso en JSON. */
public final class RecepcionMapper {
    private RecepcionMapper() {}

    public static RegistrarRecepcionCommand aRegistrar(RecepcionDto.Crear input, Actor actor) {
        return new RegistrarRecepcionCommand(input.idProveedor(),input.pesoBrutoKg(),input.taraKg(),
            input.pesoSacosKg(),input.humedadPct(),input.mermaLimpiezaKg(),actor);
    }

    public static CorregirRecepcionCommand aCorregir(long id, RecepcionDto.Crear input, Actor actor) {
        return new CorregirRecepcionCommand(id,input.idProveedor(),input.pesoBrutoKg(),input.taraKg(),
            input.pesoSacosKg(),input.humedadPct(),input.mermaLimpiezaKg(),actor);
    }

    public static DecidirRecepcionCommand aDecidir(long id, RecepcionDto.Decision input, Actor actor) {
        return new DecidirRecepcionCommand(id,DecisionRecepcion.valueOf(input.decision()),input.motivo(),actor);
    }

    public static RecepcionDto.Respuesta aRespuesta(RecepcionResponse recepcion) {
        return new RecepcionDto.Respuesta(recepcion.idRecepcion(),recepcion.fechaHora(),
            recepcion.idProveedor(),recepcion.proveedor(),recepcion.pesoBrutoKg(),
            recepcion.taraKg(),recepcion.pesoSacosKg(),recepcion.pesoNetoKg(),
            recepcion.humedadPct(),recepcion.mermaLimpiezaKg(),recepcion.estado().name(),
            recepcion.entradaKardexKg(),recepcion.registradoPor(),recepcion.autorizadoPor());
    }
}
