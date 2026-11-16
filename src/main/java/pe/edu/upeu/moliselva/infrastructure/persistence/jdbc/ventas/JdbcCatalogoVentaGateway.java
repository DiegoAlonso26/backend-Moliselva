package pe.edu.upeu.moliselva.infrastructure.persistence.jdbc.ventas;

import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.operaciones.OperacionRepository;
import pe.edu.upeu.moliselva.application.ventas.gateway.CatalogoVentaGateway;

@Repository
public class JdbcCatalogoVentaGateway implements CatalogoVentaGateway {
    private final OperacionRepository db;
    public JdbcCatalogoVentaGateway(OperacionRepository db) { this.db=db; }
    @Override public boolean clienteActivo(long id) {
        return db.one("SELECT 1 FROM cliente WHERE id_cliente=? AND activo=true",id).isPresent();
    }
    @Override public boolean productoActivo(long id) {
        return db.one("SELECT 1 FROM producto WHERE id_producto=? AND activo=true",id).isPresent();
    }
}
