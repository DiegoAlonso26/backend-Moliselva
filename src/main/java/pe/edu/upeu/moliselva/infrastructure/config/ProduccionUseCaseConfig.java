package pe.edu.upeu.moliselva.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.edu.upeu.moliselva.application.produccion.gateway.BitacoraProduccionGateway;
import pe.edu.upeu.moliselva.application.produccion.gateway.CatalogoProduccionGateway;
import pe.edu.upeu.moliselva.application.produccion.gateway.InventarioProduccionGateway;
import pe.edu.upeu.moliselva.application.produccion.gateway.LoteGateway;
import pe.edu.upeu.moliselva.application.produccion.usecase.CambiarEstadoLote;
import pe.edu.upeu.moliselva.application.produccion.usecase.CambiarEstadoLoteUseCase;
import pe.edu.upeu.moliselva.application.produccion.usecase.ConsultarLotes;
import pe.edu.upeu.moliselva.application.produccion.usecase.ConsultarLotesUseCase;
import pe.edu.upeu.moliselva.application.produccion.usecase.FinalizarLote;
import pe.edu.upeu.moliselva.application.produccion.usecase.FinalizarLoteUseCase;
import pe.edu.upeu.moliselva.application.produccion.usecase.GestionarInsumosLote;
import pe.edu.upeu.moliselva.application.produccion.usecase.GestionarInsumosLoteUseCase;
import pe.edu.upeu.moliselva.application.produccion.usecase.PlanificarLote;
import pe.edu.upeu.moliselva.application.produccion.usecase.PlanificarLoteUseCase;

/**
 * Composition root de producción por lote. Finalizar sigue siendo una sola transacción:
 * salidas de insumos, entrada del producto terminado, estado del lote y bitácora.
 */
@Configuration
public class ProduccionUseCaseConfig {
    private final Transacciones tx;

    public ProduccionUseCaseConfig(Transacciones tx) { this.tx = tx; }

    @Bean
    ConsultarLotesUseCase consultarLotes(LoteGateway lotes) {
        return tx.envolver(ConsultarLotesUseCase.class, new ConsultarLotes(lotes));
    }

    @Bean
    PlanificarLoteUseCase planificarLote(LoteGateway lotes, CatalogoProduccionGateway catalogo,
            InventarioProduccionGateway inventario, BitacoraProduccionGateway bitacora) {
        return tx.envolver(PlanificarLoteUseCase.class, new PlanificarLote(lotes,catalogo,inventario,bitacora));
    }

    @Bean
    GestionarInsumosLoteUseCase gestionarInsumosLote(LoteGateway lotes, CatalogoProduccionGateway catalogo,
            InventarioProduccionGateway inventario, BitacoraProduccionGateway bitacora) {
        return tx.envolver(GestionarInsumosLoteUseCase.class,
            new GestionarInsumosLote(lotes,catalogo,inventario,bitacora));
    }

    @Bean
    CambiarEstadoLoteUseCase cambiarEstadoLote(LoteGateway lotes, InventarioProduccionGateway inventario,
            BitacoraProduccionGateway bitacora) {
        return tx.envolver(CambiarEstadoLoteUseCase.class, new CambiarEstadoLote(lotes,inventario,bitacora));
    }

    @Bean
    FinalizarLoteUseCase finalizarLote(LoteGateway lotes, CatalogoProduccionGateway catalogo,
            InventarioProduccionGateway inventario, BitacoraProduccionGateway bitacora) {
        return tx.envolver(FinalizarLoteUseCase.class, new FinalizarLote(lotes,catalogo,inventario,bitacora));
    }
}
