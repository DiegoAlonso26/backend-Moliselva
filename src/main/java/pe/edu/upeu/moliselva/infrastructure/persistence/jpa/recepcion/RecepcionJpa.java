package pe.edu.upeu.moliselva.infrastructure.persistence.jpa.recepcion;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import pe.edu.upeu.moliselva.domain.recepcion.EstadoRecepcion;

/**
 * Entidad de persistencia (JPA) de la tabla {@code recepcion}. No es la entidad del
 * dominio: solo refleja columnas. {@link RecepcionJpaMapper} la convierte en
 * {@code domain.recepcion.Recepcion} y viceversa.
 * <p>
 * {@code peso_neto_kg} (columna generada) y {@code fecha_hora} (valor por defecto) los
 * calcula PostgreSQL, por eso no se escriben. Las lecturas completas van por SQL.
 */
@Entity
@Table(name = "recepcion")
public class RecepcionJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recepcion")
    private Long id;

    @Column(name = "id_proveedor", nullable = false)
    private Long idProveedor;

    @Column(name = "id_insumo", nullable = false)
    private Long idInsumo;

    @Column(name = "id_registra", nullable = false)
    private Long idRegistra;

    @Column(name = "peso_bruto_kg", nullable = false, precision = 12, scale = 2)
    private BigDecimal pesoBrutoKg;

    @Column(name = "tara_kg", nullable = false, precision = 12, scale = 2)
    private BigDecimal taraKg;

    @Column(name = "peso_sacos_kg", nullable = false, precision = 12, scale = 2)
    private BigDecimal pesoSacosKg;

    @Column(name = "peso_neto_kg", insertable = false, updatable = false, precision = 12, scale = 2)
    private BigDecimal pesoNetoKg;

    @Column(name = "merma_limpieza_kg", nullable = false, precision = 12, scale = 2)
    private BigDecimal mermaLimpiezaKg;

    @Column(name = "humedad_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal humedadPct;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoRecepcion estado;

    @Column(name = "id_autoriza")
    private Long idAutoriza;

    protected RecepcionJpa() {}

    RecepcionJpa(Long idInsumo, Long idRegistra) {
        this.idInsumo = idInsumo;
        this.idRegistra = idRegistra;
    }

    Long getId() { return id; }
    Long getIdProveedor() { return idProveedor; }
    Long getIdInsumo() { return idInsumo; }
    Long getIdRegistra() { return idRegistra; }
    BigDecimal getPesoBrutoKg() { return pesoBrutoKg; }
    BigDecimal getTaraKg() { return taraKg; }
    BigDecimal getPesoSacosKg() { return pesoSacosKg; }
    BigDecimal getMermaLimpiezaKg() { return mermaLimpiezaKg; }
    BigDecimal getHumedadPct() { return humedadPct; }
    EstadoRecepcion getEstado() { return estado; }
    Long getIdAutoriza() { return idAutoriza; }

    void setIdProveedor(Long idProveedor) { this.idProveedor = idProveedor; }
    void setPesoBrutoKg(BigDecimal valor) { this.pesoBrutoKg = valor; }
    void setTaraKg(BigDecimal valor) { this.taraKg = valor; }
    void setPesoSacosKg(BigDecimal valor) { this.pesoSacosKg = valor; }
    void setMermaLimpiezaKg(BigDecimal valor) { this.mermaLimpiezaKg = valor; }
    void setHumedadPct(BigDecimal valor) { this.humedadPct = valor; }
    void setEstado(EstadoRecepcion estado) { this.estado = estado; }
    void setIdAutoriza(Long idAutoriza) { this.idAutoriza = idAutoriza; }
}
