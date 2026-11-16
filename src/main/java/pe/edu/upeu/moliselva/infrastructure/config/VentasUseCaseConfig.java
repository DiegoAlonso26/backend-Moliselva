package pe.edu.upeu.moliselva.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.edu.upeu.moliselva.application.ventas.gateway.BitacoraVentaGateway;
import pe.edu.upeu.moliselva.application.ventas.gateway.CatalogoVentaGateway;
import pe.edu.upeu.moliselva.application.ventas.gateway.DespachoGateway;
import pe.edu.upeu.moliselva.application.ventas.gateway.PedidoGateway;
import pe.edu.upeu.moliselva.application.ventas.usecase.ConfirmarDespacho;
import pe.edu.upeu.moliselva.application.ventas.usecase.ConfirmarDespachoUseCase;
import pe.edu.upeu.moliselva.application.ventas.usecase.ConsultarDespachos;
import pe.edu.upeu.moliselva.application.ventas.usecase.ConsultarDespachosUseCase;
import pe.edu.upeu.moliselva.application.ventas.usecase.ConsultarPedidos;
import pe.edu.upeu.moliselva.application.ventas.usecase.ConsultarPedidosUseCase;
import pe.edu.upeu.moliselva.application.ventas.usecase.GestionarDespachos;
import pe.edu.upeu.moliselva.application.ventas.usecase.GestionarDespachosUseCase;
import pe.edu.upeu.moliselva.application.ventas.usecase.GestionarDetallesDespacho;
import pe.edu.upeu.moliselva.application.ventas.usecase.GestionarDetallesDespachoUseCase;
import pe.edu.upeu.moliselva.application.ventas.usecase.GestionarDetallesPedido;
import pe.edu.upeu.moliselva.application.ventas.usecase.GestionarDetallesPedidoUseCase;
import pe.edu.upeu.moliselva.application.ventas.usecase.GestionarPedidos;
import pe.edu.upeu.moliselva.application.ventas.usecase.GestionarPedidosUseCase;

/**
 * Composition root de pedidos y despachos. Confirmar un despacho sigue siendo una sola
 * transacción: bloqueo de despacho y pedido, salidas al kardex, estados y bitácora.
 */
@Configuration
public class VentasUseCaseConfig {
    private final Transacciones tx;

    public VentasUseCaseConfig(Transacciones tx) { this.tx = tx; }

    @Bean
    ConsultarPedidosUseCase consultarPedidos(PedidoGateway pedidos) {
        return tx.envolver(ConsultarPedidosUseCase.class, new ConsultarPedidos(pedidos));
    }

    @Bean
    GestionarPedidosUseCase gestionarPedidos(PedidoGateway pedidos, CatalogoVentaGateway catalogo,
            BitacoraVentaGateway bitacora) {
        return tx.envolver(GestionarPedidosUseCase.class, new GestionarPedidos(pedidos,catalogo,bitacora));
    }

    @Bean
    GestionarDetallesPedidoUseCase gestionarDetallesPedido(PedidoGateway pedidos, CatalogoVentaGateway catalogo,
            BitacoraVentaGateway bitacora) {
        return tx.envolver(GestionarDetallesPedidoUseCase.class,
            new GestionarDetallesPedido(pedidos,catalogo,bitacora));
    }

    @Bean
    ConsultarDespachosUseCase consultarDespachos(DespachoGateway despachos) {
        return tx.envolver(ConsultarDespachosUseCase.class, new ConsultarDespachos(despachos));
    }

    @Bean
    GestionarDespachosUseCase gestionarDespachos(DespachoGateway despachos, PedidoGateway pedidos,
            BitacoraVentaGateway bitacora) {
        return tx.envolver(GestionarDespachosUseCase.class, new GestionarDespachos(despachos,pedidos,bitacora));
    }

    @Bean
    GestionarDetallesDespachoUseCase gestionarDetallesDespacho(DespachoGateway despachos,
            CatalogoVentaGateway catalogo, BitacoraVentaGateway bitacora) {
        return tx.envolver(GestionarDetallesDespachoUseCase.class,
            new GestionarDetallesDespacho(despachos,catalogo,bitacora));
    }

    @Bean
    ConfirmarDespachoUseCase confirmarDespacho(DespachoGateway despachos, PedidoGateway pedidos,
            BitacoraVentaGateway bitacora) {
        return tx.envolver(ConfirmarDespachoUseCase.class, new ConfirmarDespacho(despachos,pedidos,bitacora));
    }
}
