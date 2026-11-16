package pe.edu.upeu.moliselva.domain.recepcion;

import java.util.Objects;
import pe.edu.upeu.moliselva.domain.shared.Kilogramos;

/**
 * Entidad (raíz de agregado): una recepción de maíz en la planta. Tiene identidad (su id)
 * y un ciclo de vida que solo cambia a través de sus métodos:
 * <pre>
 *   registrar ──► ACEPTADA                       (ingresa al kardex)
 *            ├──► PENDIENTE ──decidir──► ACEPTADA | RECHAZADA
 *            └──► RECHAZADA
 *   PENDIENTE o RECHAZADA sin decisión del jefe: se puede corregir o eliminar.
 * </pre>
 * Dos recepciones son la misma si tienen el mismo id, aunque cambien sus datos.
 */
public final class Recepcion {
    private final Long id;
    private final long idInsumo;
    private final long idRegistra;
    private long idProveedor;
    private PesajeMaiz pesaje;
    private PorcentajeHumedad humedad;
    private EstadoRecepcion estado;
    private Long idAutoriza;

    private Recepcion(Long id, long idProveedor, long idInsumo, long idRegistra, PesajeMaiz pesaje,
            PorcentajeHumedad humedad, EstadoRecepcion estado, Long idAutoriza) {
        this.id = id;
        this.idProveedor = idProveedor;
        this.idInsumo = idInsumo;
        this.idRegistra = idRegistra;
        this.pesaje = Objects.requireNonNull(pesaje);
        this.humedad = Objects.requireNonNull(humedad);
        this.estado = Objects.requireNonNull(estado);
        this.idAutoriza = idAutoriza;
    }

    /** Nueva recepción: el estado inicial lo decide la humedad. Aún no tiene id. */
    public static Recepcion registrar(long idProveedor, long idInsumo, long idRegistra,
            PesajeMaiz pesaje, PorcentajeHumedad humedad) {
        return new Recepcion(null, idProveedor, idInsumo, idRegistra, pesaje, humedad,
            humedad.estadoInicial(), null);
    }

    /** Reconstruye una recepción ya guardada (lo usa la persistencia). */
    public static Recepcion existente(long id, long idProveedor, long idInsumo, long idRegistra,
            PesajeMaiz pesaje, PorcentajeHumedad humedad, EstadoRecepcion estado, Long idAutoriza) {
        return new Recepcion(id, idProveedor, idInsumo, idRegistra, pesaje, humedad, estado, idAutoriza);
    }

    /** El jefe de planta acepta o rechaza una recepción PENDIENTE. */
    public void decidir(DecisionRecepcion decision, long idJefe) {
        if (estado != EstadoRecepcion.PENDIENTE) {
            throw new RecepcionInvalida(RecepcionInvalida.Regla.RECEPCION_NO_PENDIENTE,
                "La recepción ya tiene una decisión");
        }
        estado = decision == DecisionRecepcion.ACEPTAR ? EstadoRecepcion.ACEPTADA : EstadoRecepcion.RECHAZADA;
        idAutoriza = idJefe;
    }

    /** Corrige los datos de una recepción no decidida; la humedad vuelve a decidir el estado. */
    public void corregir(long nuevoProveedor, PesajeMaiz nuevoPesaje, PorcentajeHumedad nuevaHumedad) {
        verificarEditable();
        idProveedor = nuevoProveedor;
        pesaje = Objects.requireNonNull(nuevoPesaje);
        humedad = Objects.requireNonNull(nuevaHumedad);
        estado = nuevaHumedad.estadoInicial();
    }

    /** Solo una recepción no aceptada y sin decisión del jefe se puede corregir o eliminar. */
    public boolean esEditable() {
        return estado != EstadoRecepcion.ACEPTADA && idAutoriza == null;
    }

    public void verificarEditable() {
        if (!esEditable()) {
            throw new RecepcionInvalida(RecepcionInvalida.Regla.RECEPCION_NO_EDITABLE, RecepcionInvalida.NO_EDITABLE);
        }
    }

    /** Una recepción aceptada ingresa al kardex de maíz. */
    public boolean ingresaAlKardex() { return estado == EstadoRecepcion.ACEPTADA; }

    public Kilogramos entradaKardex() { return pesaje.entradaKardex(); }

    public Long id() { return id; }
    public long idProveedor() { return idProveedor; }
    public long idInsumo() { return idInsumo; }
    public long idRegistra() { return idRegistra; }
    public PesajeMaiz pesaje() { return pesaje; }
    public PorcentajeHumedad humedad() { return humedad; }
    public EstadoRecepcion estado() { return estado; }
    public Long idAutoriza() { return idAutoriza; }

    @Override
    public boolean equals(Object otro) {
        return otro instanceof Recepcion r && id != null && id.equals(r.id);
    }

    @Override
    public int hashCode() { return id == null ? System.identityHashCode(this) : id.hashCode(); }
}
