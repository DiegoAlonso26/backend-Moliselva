package pe.edu.upeu.moliselva.infrastructure.persistence.jpa.inventario;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/**
 * Movimiento del kardex ({@code movimiento_inventario}). El kardex es inmutable (lo
 * protege el trigger {@code trg_kardex_no_update}): esta entidad solo se inserta y
 * Hibernate nunca la actualiza. Las columnas no mapeadas usan su valor por defecto o NULL.
 */
@Entity
@Immutable
@Table(name = "movimiento_inventario")
public class MovimientoInventarioJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Long id;

    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private String tipoMovimiento;

    @Column(name = "id_insumo")
    private Long idInsumo;

    @Column(name = "cantidad", nullable = false, precision = 14, scale = 3)
    private BigDecimal cantidad;

    @Column(name = "unidad_medida", nullable = false, length = 20)
    private String unidadMedida;

    @Column(name = "id_recepcion")
    private Long idRecepcion;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    protected MovimientoInventarioJpa() {}

    /** Entrada de maíz al kardex por una recepción aceptada. */
    static MovimientoInventarioJpa entradaPorRecepcion(long idInsumo, long idRecepcion, BigDecimal kg, long idUsuario) {
        var m = new MovimientoInventarioJpa();
        m.tipoMovimiento = "ENTRADA";
        m.idInsumo = idInsumo;
        m.cantidad = kg;
        m.unidadMedida = "kg";
        m.idRecepcion = idRecepcion;
        m.idUsuario = idUsuario;
        return m;
    }
}
