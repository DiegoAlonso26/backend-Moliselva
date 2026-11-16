package pe.edu.upeu.moliselva.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;

/**
 * Composition root de PO-01: crea los casos de uso (Java puro) y los envuelve en una
 * transacción con {@link Transacciones}. Registrar, decidir y corregir siguen siendo
 * una sola transacción (recepción, entrada al kardex y bitácora).
 */
@Configuration
public class Po01UseCaseConfig {
    private final Transacciones tx;

    public Po01UseCaseConfig(Transacciones tx) { this.tx = tx; }

    @Bean
    RegistrarRecepcionUseCase registrarRecepcion(RecepcionGateway recepciones, ProveedorActivoGateway proveedores,
            EntradaKardexGateway kardex, BitacoraGateway bitacora) {
        return tx.envolver(RegistrarRecepcionUseCase.class,
            new RegistrarRecepcion(recepciones,proveedores,kardex,bitacora));
    }

    @Bean
    DecidirRecepcionUseCase decidirRecepcion(RecepcionGateway recepciones, EntradaKardexGateway kardex,
            BitacoraGateway bitacora) {
        return tx.envolver(DecidirRecepcionUseCase.class, new DecidirRecepcion(recepciones,kardex,bitacora));
    }

    @Bean
    CorregirRecepcionUseCase corregirRecepcion(RecepcionGateway recepciones, ProveedorActivoGateway proveedores,
            EntradaKardexGateway kardex, BitacoraGateway bitacora) {
        return tx.envolver(CorregirRecepcionUseCase.class,
            new CorregirRecepcion(recepciones,proveedores,kardex,bitacora));
    }

    @Bean
    EliminarRecepcionUseCase eliminarRecepcion(RecepcionGateway recepciones, BitacoraGateway bitacora) {
        return tx.envolver(EliminarRecepcionUseCase.class, new EliminarRecepcion(recepciones,bitacora));
    }

    @Bean
    ConsultarRecepcionUseCase consultarRecepcion(RecepcionGateway recepciones) {
        return tx.envolver(ConsultarRecepcionUseCase.class, new ConsultarRecepcion(recepciones));
    }

    @Bean
    ConsultarProveedoresUseCase consultarProveedores(ProveedorGateway proveedores) {
        return tx.envolver(ConsultarProveedoresUseCase.class, new ConsultarProveedores(proveedores));
    }

    @Bean
    GestionarProveedorUseCase gestionarProveedor(ProveedorGateway proveedores, BitacoraGateway bitacora) {
        return tx.envolver(GestionarProveedorUseCase.class, new GestionarProveedor(proveedores,bitacora));
    }

    @Bean
    ConsultarKardexMaizUseCase consultarKardexMaiz(KardexMaizGateway kardex) {
        return tx.envolver(ConsultarKardexMaizUseCase.class, new ConsultarKardexMaiz(kardex));
    }

    @Bean
    ExportarKardexMaizUseCase exportarKardexMaiz(KardexMaizGateway kardex, BitacoraGateway bitacora) {
        return tx.envolver(ExportarKardexMaizUseCase.class,
            new ExportarKardexMaiz(new ConsultarKardexMaiz(kardex),bitacora));
    }
}
