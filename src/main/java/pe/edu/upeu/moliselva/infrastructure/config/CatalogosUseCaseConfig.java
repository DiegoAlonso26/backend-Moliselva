package pe.edu.upeu.moliselva.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.edu.upeu.moliselva.application.bitacora.gateway.BitacoraConsultaGateway;
import pe.edu.upeu.moliselva.application.bitacora.usecase.ConsultarBitacora;
import pe.edu.upeu.moliselva.application.bitacora.usecase.ConsultarBitacoraUseCase;
import pe.edu.upeu.moliselva.application.formulas.gateway.FormulaGateway;
import pe.edu.upeu.moliselva.application.formulas.usecase.ConsultarFormulas;
import pe.edu.upeu.moliselva.application.formulas.usecase.ConsultarFormulasUseCase;
import pe.edu.upeu.moliselva.application.formulas.usecase.GestionarDetallesFormula;
import pe.edu.upeu.moliselva.application.formulas.usecase.GestionarDetallesFormulaUseCase;
import pe.edu.upeu.moliselva.application.formulas.usecase.GestionarFormulas;
import pe.edu.upeu.moliselva.application.formulas.usecase.GestionarFormulasUseCase;
import pe.edu.upeu.moliselva.application.inventario.gateway.InventarioGateway;
import pe.edu.upeu.moliselva.application.inventario.usecase.ConsultarInventario;
import pe.edu.upeu.moliselva.application.inventario.usecase.ConsultarInventarioUseCase;
import pe.edu.upeu.moliselva.application.inventario.usecase.RegistrarAjuste;
import pe.edu.upeu.moliselva.application.inventario.usecase.RegistrarAjusteUseCase;
import pe.edu.upeu.moliselva.application.maestros.gateway.ClienteGateway;
import pe.edu.upeu.moliselva.application.maestros.gateway.InsumoGateway;
import pe.edu.upeu.moliselva.application.maestros.gateway.ProductoGateway;
import pe.edu.upeu.moliselva.application.maestros.gateway.StockItemGateway;
import pe.edu.upeu.moliselva.application.maestros.usecase.ConsultarMaestros;
import pe.edu.upeu.moliselva.application.maestros.usecase.ConsultarMaestrosUseCase;
import pe.edu.upeu.moliselva.application.maestros.usecase.GestionarClientes;
import pe.edu.upeu.moliselva.application.maestros.usecase.GestionarClientesUseCase;
import pe.edu.upeu.moliselva.application.maestros.usecase.GestionarInsumos;
import pe.edu.upeu.moliselva.application.maestros.usecase.GestionarInsumosUseCase;
import pe.edu.upeu.moliselva.application.maestros.usecase.GestionarProductos;
import pe.edu.upeu.moliselva.application.maestros.usecase.GestionarProductosUseCase;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;

/** Composition root de maestros, fórmulas, inventario general y consulta de bitácora. */
@Configuration
public class CatalogosUseCaseConfig {
    private final Transacciones tx;

    public CatalogosUseCaseConfig(Transacciones tx) { this.tx = tx; }

    @Bean
    ConsultarMaestrosUseCase consultarMaestros(ClienteGateway clientes, InsumoGateway insumos, ProductoGateway productos) {
        return tx.envolver(ConsultarMaestrosUseCase.class, new ConsultarMaestros(clientes,insumos,productos));
    }

    @Bean
    GestionarClientesUseCase gestionarClientes(ClienteGateway clientes, BitacoraGateway bitacora) {
        return tx.envolver(GestionarClientesUseCase.class, new GestionarClientes(clientes,bitacora));
    }

    @Bean
    GestionarInsumosUseCase gestionarInsumos(InsumoGateway insumos, StockItemGateway stock, BitacoraGateway bitacora) {
        return tx.envolver(GestionarInsumosUseCase.class, new GestionarInsumos(insumos,stock,bitacora));
    }

    @Bean
    GestionarProductosUseCase gestionarProductos(ProductoGateway productos, StockItemGateway stock, BitacoraGateway bitacora) {
        return tx.envolver(GestionarProductosUseCase.class, new GestionarProductos(productos,stock,bitacora));
    }

    @Bean
    ConsultarFormulasUseCase consultarFormulas(FormulaGateway formulas) {
        return tx.envolver(ConsultarFormulasUseCase.class, new ConsultarFormulas(formulas));
    }

    @Bean
    GestionarFormulasUseCase gestionarFormulas(FormulaGateway formulas, BitacoraGateway bitacora) {
        return tx.envolver(GestionarFormulasUseCase.class, new GestionarFormulas(formulas,bitacora));
    }

    @Bean
    GestionarDetallesFormulaUseCase gestionarDetallesFormula(FormulaGateway formulas, BitacoraGateway bitacora) {
        return tx.envolver(GestionarDetallesFormulaUseCase.class, new GestionarDetallesFormula(formulas,bitacora));
    }

    @Bean
    ConsultarInventarioUseCase consultarInventario(InventarioGateway inventario) {
        return tx.envolver(ConsultarInventarioUseCase.class, new ConsultarInventario(inventario));
    }

    @Bean
    RegistrarAjusteUseCase registrarAjuste(InventarioGateway inventario, BitacoraGateway bitacora) {
        return tx.envolver(RegistrarAjusteUseCase.class, new RegistrarAjuste(inventario,bitacora));
    }

    @Bean
    ConsultarBitacoraUseCase consultarBitacora(BitacoraConsultaGateway bitacora) {
        return tx.envolver(ConsultarBitacoraUseCase.class, new ConsultarBitacora(bitacora));
    }
}
