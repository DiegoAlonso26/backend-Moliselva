package pe.edu.upeu.moliselva.application.recepcion;

import java.util.function.Supplier;
import pe.edu.upeu.moliselva.application.recepcion.gateway.EntradaKardexGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.ProveedorActivoGateway;
import pe.edu.upeu.moliselva.application.shared.Actor;
import pe.edu.upeu.moliselva.domain.recepcion.RecepcionInvalida;

/** Comprobaciones de aplicación que comparten los handlers de recepción: permisos y referencias. */
public final class ReglasRecepcion {
    private ReglasRecepcion() {}

    public static void puedeRegistrar(Actor actor) {
        if (!"OPERARIO".equals(actor.rol()) && !"JEFE_PLANTA".equals(actor.rol())) {
            throw new RecepcionFailure(RecepcionFailure.Reason.SIN_PERMISO,
                "No tiene permiso para registrar o corregir recepciones");
        }
    }

    public static void puedeDecidir(Actor actor) {
        if (!"JEFE_PLANTA".equals(actor.rol())) {
            throw new RecepcionFailure(RecepcionFailure.Reason.SIN_PERMISO,
                "Solo el jefe de planta puede decidir recepciones pendientes");
        }
    }

    public static void proveedorActivo(ProveedorActivoGateway proveedores, long id) {
        if (!proveedores.existeActivo(id)) {
            throw new RecepcionFailure(RecepcionFailure.Reason.PROVEEDOR_INVALIDO,
                "Proveedor inexistente o inactivo");
        }
    }

    public static long maizId(EntradaKardexGateway kardex) {
        return kardex.maizId().orElseThrow(() ->
            new RecepcionFailure(RecepcionFailure.Reason.MAIZ_NO_CONFIGURADO,
                "No existe el insumo Maíz activo"));
    }

    public static RecepcionFailure noEncontrada() {
        return new RecepcionFailure(RecepcionFailure.Reason.RECEPCION_NO_ENCONTRADA, "La recepción no existe");
    }

    /** Ejecuta una operación del dominio y traduce sus reglas incumplidas a {@link RecepcionFailure}. */
    public static <T> T dominio(Supplier<T> operacion) {
        try {
            return operacion.get();
        } catch (RecepcionInvalida regla) {
            throw RecepcionFailure.de(regla);
        }
    }

    public static void dominio(Runnable operacion) {
        dominio(() -> { operacion.run(); return null; });
    }
}
