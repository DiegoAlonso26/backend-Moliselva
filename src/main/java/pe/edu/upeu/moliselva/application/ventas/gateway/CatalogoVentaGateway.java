package pe.edu.upeu.moliselva.application.ventas.gateway;

public interface CatalogoVentaGateway {
    boolean clienteActivo(long id);
    boolean productoActivo(long id);
}
