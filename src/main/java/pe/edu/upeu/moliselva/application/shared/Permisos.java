package pe.edu.upeu.moliselva.application.shared;

import java.util.Set;

/** Roles de Moliselva y comprobación de permisos dentro de los casos de uso. */
public final class Permisos {
    public static final String ADMINISTRADOR = "ADMINISTRADOR";
    public static final String JEFE_PLANTA = "JEFE_PLANTA";
    public static final String OPERARIO = "OPERARIO";
    public static final String CONTADORA = "CONTADORA";

    private Permisos() {}

    public static boolean tieneRol(Actor actor, String... roles) {
        return actor != null && Set.of(roles).contains(actor.rol());
    }

    public static void exigir(Actor actor, String mensaje, String... roles) {
        if (!tieneRol(actor, roles)) {
            throw new SinPermiso(mensaje);
        }
    }

    public static final class SinPermiso extends FallaNegocio {
        public SinPermiso(String mensaje) { super(mensaje); }
        @Override public Tipo tipo() { return Tipo.SIN_PERMISO; }
        @Override public String codigo() { return "SIN_PERMISO"; }
    }
}
