package pe.edu.upeu.moliselva.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.edu.upeu.moliselva.application.inventario.gateway.KardexMaizConsultaGateway;
import pe.edu.upeu.moliselva.application.inventario.kardex.exportar.ExportarKardexMaizHandler;
import pe.edu.upeu.moliselva.application.inventario.kardex.listar.ListarKardexMaizHandler;
import pe.edu.upeu.moliselva.application.inventario.kardex.stock.ConsultarStockMaizHandler;
import pe.edu.upeu.moliselva.application.proveedor.gateway.ProveedorGateway;
import pe.edu.upeu.moliselva.application.proveedor.usecase.ConsultarProveedores;
import pe.edu.upeu.moliselva.application.proveedor.usecase.ConsultarProveedoresUseCase;
import pe.edu.upeu.moliselva.application.proveedor.usecase.GestionarProveedor;
import pe.edu.upeu.moliselva.application.proveedor.usecase.GestionarProveedorUseCase;
import pe.edu.upeu.moliselva.application.recepcion.corregir.CorregirRecepcionHandler;
import pe.edu.upeu.moliselva.application.recepcion.decidir.DecidirRecepcionHandler;
import pe.edu.upeu.moliselva.application.recepcion.eliminar.EliminarRecepcionHandler;
import pe.edu.upeu.moliselva.application.recepcion.gateway.EntradaKardexGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.ProveedorActivoGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.RecepcionConsultaGateway;
import pe.edu.upeu.moliselva.application.recepcion.gateway.RecepcionGateway;
import pe.edu.upeu.moliselva.application.recepcion.listar.ListarRecepcionesHandler;
import pe.edu.upeu.moliselva.application.recepcion.obtener.ObtenerRecepcionHandler;
import pe.edu.upeu.moliselva.application.recepcion.registrar.RegistrarRecepcionHandler;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;

/**
 * Composition root de PO-01. Crea los handlers (Java puro) como beans; el
 * {@code MediadorSpring} los registra por el tipo de Command o Query que atienden y les
 * aplica la transacción. Cada método devuelve la clase concreta para que Spring conserve
 * el tipo genérico del handler.
 * <p>
 * Proveedores es un módulo simple: sigue con casos de uso envueltos por {@link Transacciones}.
 */
@Configuration
public class Po01UseCaseConfig {
    private final Transacciones tx;

    public Po01UseCaseConfig(Transacciones tx) { this.tx = tx; }

    @Bean
    RegistrarRecepcionHandler registrarRecepcionHandler(RecepcionGateway recepciones, RecepcionConsultaGateway consultas,
            ProveedorActivoGateway proveedores, EntradaKardexGateway kardex, BitacoraGateway bitacora) {
        return new RegistrarRecepcionHandler(recepciones,consultas,proveedores,kardex,bitacora);
    }

    @Bean
    CorregirRecepcionHandler corregirRecepcionHandler(RecepcionGateway recepciones, RecepcionConsultaGateway consultas,
            ProveedorActivoGateway proveedores, EntradaKardexGateway kardex, BitacoraGateway bitacora) {
        return new CorregirRecepcionHandler(recepciones,consultas,proveedores,kardex,bitacora);
    }

    @Bean
    DecidirRecepcionHandler decidirRecepcionHandler(RecepcionGateway recepciones, RecepcionConsultaGateway consultas,
            EntradaKardexGateway kardex, BitacoraGateway bitacora) {
        return new DecidirRecepcionHandler(recepciones,consultas,kardex,bitacora);
    }

    @Bean
    EliminarRecepcionHandler eliminarRecepcionHandler(RecepcionGateway recepciones, BitacoraGateway bitacora) {
        return new EliminarRecepcionHandler(recepciones,bitacora);
    }

    @Bean
    ListarRecepcionesHandler listarRecepcionesHandler(RecepcionConsultaGateway consultas) {
        return new ListarRecepcionesHandler(consultas);
    }

    @Bean
    ObtenerRecepcionHandler obtenerRecepcionHandler(RecepcionConsultaGateway consultas) {
        return new ObtenerRecepcionHandler(consultas);
    }

    @Bean
    ListarKardexMaizHandler listarKardexMaizHandler(KardexMaizConsultaGateway kardex) {
        return new ListarKardexMaizHandler(kardex);
    }

    @Bean
    ConsultarStockMaizHandler consultarStockMaizHandler(KardexMaizConsultaGateway kardex) {
        return new ConsultarStockMaizHandler(kardex);
    }

    @Bean
    ExportarKardexMaizHandler exportarKardexMaizHandler(KardexMaizConsultaGateway kardex, BitacoraGateway bitacora) {
        return new ExportarKardexMaizHandler(kardex,bitacora);
    }

    @Bean
    ConsultarProveedoresUseCase consultarProveedores(ProveedorGateway proveedores) {
        return tx.envolver(ConsultarProveedoresUseCase.class, new ConsultarProveedores(proveedores));
    }

    @Bean
    GestionarProveedorUseCase gestionarProveedor(ProveedorGateway proveedores, BitacoraGateway bitacora) {
        return tx.envolver(GestionarProveedorUseCase.class, new GestionarProveedor(proveedores,bitacora));
    }
}
