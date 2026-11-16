package pe.edu.upeu.moliselva.application.recepcion.usecase;

import pe.edu.upeu.moliselva.application.shared.Actor;

import pe.edu.upeu.moliselva.domain.recepcion.RecepcionBloqueo;
import pe.edu.upeu.moliselva.domain.recepcion.RecepcionDatos;
import pe.edu.upeu.moliselva.domain.recepcion.RecepcionPolicy;
import pe.edu.upeu.moliselva.application.recepcion.gateway.EntradaKardexGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.ProveedorActivoGateway;

final class RecepcionChecks {
    private RecepcionChecks() {}
    static void puedeRegistrar(Actor actor) {
        if (!"OPERARIO".equals(actor.rol()) && !"JEFE_PLANTA".equals(actor.rol())) {
            throw new RecepcionFailure(RecepcionFailure.Reason.SIN_PERMISO,
                "No tiene permiso para registrar o corregir recepciones");
        }
    }
    static void puedeDecidir(Actor actor) {
        if (!"JEFE_PLANTA".equals(actor.rol())) {
            throw new RecepcionFailure(RecepcionFailure.Reason.SIN_PERMISO,
                "Solo el jefe de planta puede decidir recepciones pendientes");
        }
    }
    static void editable(RecepcionBloqueo row) {
        if (!row.editable()) {
            throw new RecepcionFailure(RecepcionFailure.Reason.RECEPCION_NO_EDITABLE,
                "La recepción aceptada o decidida no se puede modificar ni eliminar");
        }
    }
    static RecepcionFailure missing() {
        return new RecepcionFailure(RecepcionFailure.Reason.RECEPCION_NO_ENCONTRADA,
            "La recepción no existe");
    }
    static RecepcionFailure notEditable() {
        return new RecepcionFailure(RecepcionFailure.Reason.RECEPCION_NO_EDITABLE,
            "La recepción aceptada o decidida no se puede modificar ni eliminar");
    }
    static RecepcionFailure invalid(IllegalArgumentException ex) {
        var reason=ex.getMessage()!=null && ex.getMessage().contains("humedad")
            ? RecepcionFailure.Reason.HUMEDAD_FUERA_DE_RANGO
            : RecepcionFailure.Reason.PESOS_INVALIDOS;
        return new RecepcionFailure(reason,ex.getMessage());
    }
    static void proveedorActivo(ProveedorActivoGateway proveedores, long id) {
        if (!proveedores.existeActivo(id)) {
            throw new RecepcionFailure(RecepcionFailure.Reason.PROVEEDOR_INVALIDO,
                "Proveedor inexistente o inactivo");
        }
    }
    static long maizId(EntradaKardexGateway inventario) {
        return inventario.maizId().orElseThrow(() ->
            new RecepcionFailure(RecepcionFailure.Reason.MAIZ_NO_CONFIGURADO,
                "No existe el insumo Maíz activo"));
    }
    static RecepcionPolicy.Evaluacion evaluar(RecepcionDatos datos) {
        try {
            return RecepcionPolicy.evaluar(datos);
        } catch (IllegalArgumentException ex) {
            throw invalid(ex);
        }
    }
}
