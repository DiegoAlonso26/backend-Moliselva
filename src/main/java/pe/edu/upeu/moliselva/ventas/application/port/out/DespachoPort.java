package pe.edu.upeu.moliselva.ventas.application.port.out;

import java.util.List;
import java.util.Optional;
import pe.edu.upeu.moliselva.ventas.domain.DatosDespacho;
import pe.edu.upeu.moliselva.ventas.domain.DatosDetalle;
import pe.edu.upeu.moliselva.ventas.domain.Despacho;
import pe.edu.upeu.moliselva.ventas.domain.DespachoDetalle;

public interface DespachoPort {
    List<Despacho> listar();
    Optional<Despacho> buscar(long id);
    Optional<Despacho> bloquear(long id);
    Despacho crear(DatosDespacho datos, long usuarioId);
    Despacho actualizar(long id, DatosDespacho datos);
    void eliminar(long id);
    boolean tieneMovimientos(long id);
    boolean excedePedido(long idPedido, long idDespacho);
    List<DespachoDetalle> listarDetalles(long id);
    Optional<DespachoDetalle> buscarDetalle(long id, long productoId);
    DespachoDetalle agregarDetalle(long id, DatosDetalle datos);
    DespachoDetalle actualizarDetalle(long id, long productoId, DatosDetalle datos);
    void eliminarDetalle(long id, long productoId);
    void confirmarEnBase(long id, long usuarioId);
}
