package pe.edu.upeu.moliselva.infrastructure.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pe.edu.upeu.moliselva.application.inventario.gateway.KardexMaizGateway;
import pe.edu.upeu.moliselva.application.inventario.usecase.ConsultarKardexMaiz;
import pe.edu.upeu.moliselva.application.inventario.usecase.ConsultarKardexMaizUseCase;
import pe.edu.upeu.moliselva.application.inventario.usecase.ExportarKardexMaiz;
import pe.edu.upeu.moliselva.application.inventario.usecase.ExportarKardexMaizUseCase;
import pe.edu.upeu.moliselva.application.proveedor.gateway.ProveedorGateway;
import pe.edu.upeu.moliselva.application.proveedor.usecase.ConsultarProveedores;
import pe.edu.upeu.moliselva.application.proveedor.usecase.ConsultarProveedoresUseCase;
import pe.edu.upeu.moliselva.application.proveedor.usecase.GestionarProveedor;
import pe.edu.upeu.moliselva.application.proveedor.usecase.GestionarProveedorUseCase;
import pe.edu.upeu.moliselva.application.recepcion.gateway.EntradaKardexGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.ProveedorActivoGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.RecepcionGateway;
import pe.edu.upeu.moliselva.application.recepcion.usecase.ConsultarRecepcion;
import pe.edu.upeu.moliselva.application.recepcion.usecase.ConsultarRecepcionUseCase;
import pe.edu.upeu.moliselva.application.recepcion.usecase.CorregirRecepcion;
import pe.edu.upeu.moliselva.application.recepcion.usecase.CorregirRecepcionUseCase;
import pe.edu.upeu.moliselva.application.recepcion.usecase.DecidirRecepcion;
import pe.edu.upeu.moliselva.application.recepcion.usecase.DecidirRecepcionUseCase;
import pe.edu.upeu.moliselva.application.recepcion.usecase.EliminarRecepcion;
import pe.edu.upeu.moliselva.application.recepcion.usecase.EliminarRecepcionUseCase;
import pe.edu.upeu.moliselva.application.recepcion.usecase.RegistrarRecepcion;
import pe.edu.upeu.moliselva.application.recepcion.usecase.RegistrarRecepcionUseCase;
import pe.edu.upeu.moliselva.application.shared.Actor;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.domain.inventario.MovimientoKardex;
import pe.edu.upeu.moliselva.domain.proveedor.Proveedor;
import pe.edu.upeu.moliselva.domain.proveedor.ProveedorDatos;
import pe.edu.upeu.moliselva.domain.recepcion.Recepcion;

/**
 * Composition root de PO-01. Los casos de uso son Java puro; aquí se crean y
 * se envuelven en una transacción. Cada operación de escritura (recepción,
 * entrada al kardex y bitácora) sigue siendo una sola transacción.
 */
@Configuration
public class Po01UseCaseConfig {
    private final TransactionTemplate escritura;
    private final TransactionTemplate lectura;

    public Po01UseCaseConfig(PlatformTransactionManager transacciones) {
        this.escritura = new TransactionTemplate(transacciones);
        this.lectura = new TransactionTemplate(transacciones);
        this.lectura.setReadOnly(true);
    }

    // ---- Recepción de maíz ----

    @Bean
    RegistrarRecepcionUseCase registrarRecepcion(RecepcionGateway recepciones, ProveedorActivoGateway proveedores,
            EntradaKardexGateway kardex, BitacoraGateway bitacora) {
        var caso = new RegistrarRecepcion(recepciones,proveedores,kardex,bitacora);
        return (datos,actor) -> escritura.execute(s -> caso.registrar(datos,actor));
    }

    @Bean
    DecidirRecepcionUseCase decidirRecepcion(RecepcionGateway recepciones, EntradaKardexGateway kardex,
            BitacoraGateway bitacora) {
        var caso = new DecidirRecepcion(recepciones,kardex,bitacora);
        return (id,decision,motivo,actor) -> escritura.execute(s -> caso.decidir(id,decision,motivo,actor));
    }

    @Bean
    CorregirRecepcionUseCase corregirRecepcion(RecepcionGateway recepciones, ProveedorActivoGateway proveedores,
            EntradaKardexGateway kardex, BitacoraGateway bitacora) {
        var caso = new CorregirRecepcion(recepciones,proveedores,kardex,bitacora);
        return (id,datos,actor) -> escritura.execute(s -> caso.corregir(id,datos,actor));
    }

    @Bean
    EliminarRecepcionUseCase eliminarRecepcion(RecepcionGateway recepciones, BitacoraGateway bitacora) {
        var caso = new EliminarRecepcion(recepciones,bitacora);
        return (id,actor) -> escritura.executeWithoutResult(s -> caso.eliminar(id,actor));
    }

    @Bean
    ConsultarRecepcionUseCase consultarRecepcion(RecepcionGateway recepciones) {
        var caso = new ConsultarRecepcion(recepciones);
        return new ConsultarRecepcionUseCase() {
            @Override public List<Recepcion> listar(LocalDate fecha) { return lectura.execute(s -> caso.listar(fecha)); }
            @Override public Recepcion obtener(long id) { return lectura.execute(s -> caso.obtener(id)); }
        };
    }

    // ---- Proveedores ----

    @Bean
    ConsultarProveedoresUseCase consultarProveedores(ProveedorGateway proveedores) {
        var caso = new ConsultarProveedores(proveedores);
        return new ConsultarProveedoresUseCase() {
            @Override public List<Proveedor> listar() { return lectura.execute(s -> caso.listar()); }
            @Override public Proveedor obtener(long id) { return lectura.execute(s -> caso.obtener(id)); }
        };
    }

    @Bean
    GestionarProveedorUseCase gestionarProveedor(ProveedorGateway proveedores, BitacoraGateway bitacora) {
        var caso = new GestionarProveedor(proveedores,bitacora);
        return new GestionarProveedorUseCase() {
            @Override public Proveedor crear(ProveedorDatos datos, Actor actor) {
                return escritura.execute(s -> caso.crear(datos,actor));
            }
            @Override public Proveedor actualizar(long id, ProveedorDatos datos, Actor actor) {
                return escritura.execute(s -> caso.actualizar(id,datos,actor));
            }
            @Override public void desactivar(long id, Actor actor) {
                escritura.executeWithoutResult(s -> caso.desactivar(id,actor));
            }
        };
    }

    // ---- Kardex de maíz ----

    @Bean
    ConsultarKardexMaizUseCase consultarKardexMaiz(KardexMaizGateway kardex) {
        var caso = new ConsultarKardexMaiz(kardex);
        return new ConsultarKardexMaizUseCase() {
            @Override public List<MovimientoKardex> movimientos() { return lectura.execute(s -> caso.movimientos()); }
            @Override public BigDecimal stockKg() { return lectura.execute(s -> caso.stockKg()); }
        };
    }

    @Bean
    ExportarKardexMaizUseCase exportarKardexMaiz(KardexMaizGateway kardex, BitacoraGateway bitacora) {
        var caso = new ExportarKardexMaiz(new ConsultarKardexMaiz(kardex),bitacora);
        return actor -> escritura.execute(s -> caso.exportar(actor));
    }
}
