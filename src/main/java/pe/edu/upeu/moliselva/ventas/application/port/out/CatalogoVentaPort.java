package pe.edu.upeu.moliselva.ventas.application.port.out;

public interface CatalogoVentaPort {
    boolean clienteActivo(long id);
    boolean productoActivo(long id);
}
