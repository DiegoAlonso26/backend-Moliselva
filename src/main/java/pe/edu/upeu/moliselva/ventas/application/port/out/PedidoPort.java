package pe.edu.upeu.moliselva.ventas.application.port.out;

import java.util.List;
import java.util.Optional;
import pe.edu.upeu.moliselva.ventas.domain.DatosDetalle;
import pe.edu.upeu.moliselva.ventas.domain.DatosPedido;
import pe.edu.upeu.moliselva.ventas.domain.EstadoPedido;
import pe.edu.upeu.moliselva.ventas.domain.Pedido;
import pe.edu.upeu.moliselva.ventas.domain.PedidoDetalle;

public interface PedidoPort {
    List<Pedido> listar();
    Optional<Pedido> buscar(long id);
    Optional<Pedido> bloquear(long id);
    Pedido crear(DatosPedido datos);
    Pedido actualizar(long id, DatosPedido datos);
    void eliminar(long id);
    Pedido cambiarEstado(long id, EstadoPedido estado);
    boolean tieneDespachos(long id);
    boolean tieneDetalles(long id);
    List<PedidoDetalle> listarDetalles(long id);
    Optional<PedidoDetalle> buscarDetalle(long id, long productoId);
    PedidoDetalle agregarDetalle(long id, DatosDetalle datos);
    PedidoDetalle actualizarDetalle(long id, long productoId, DatosDetalle datos);
    void eliminarDetalle(long id, long productoId);
}
