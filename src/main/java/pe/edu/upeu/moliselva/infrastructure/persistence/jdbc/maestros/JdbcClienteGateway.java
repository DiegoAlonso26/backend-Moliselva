package pe.edu.upeu.moliselva.infrastructure.persistence.jdbc.maestros;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.application.maestros.gateway.ClienteGateway;
import pe.edu.upeu.moliselva.domain.maestros.Cliente;
import pe.edu.upeu.moliselva.domain.maestros.ClienteDatos;

@Repository
public class JdbcClienteGateway implements ClienteGateway {
    private static final RowMapper<Cliente> CLIENTE = (rs,n) -> new Cliente(
        rs.getLong("id_cliente"),rs.getString("tipo_cliente"),rs.getString("nombre_razon_social"),
        rs.getString("documento"),rs.getString("telefono"),rs.getString("zona"));
    private static final String COLUMNAS = "id_cliente,tipo_cliente,nombre_razon_social,documento,telefono,zona";
    private final JdbcTemplate db;

    public JdbcClienteGateway(JdbcTemplate db) { this.db=db; }

    @Override
    public List<Cliente> activos() {
        return db.query("SELECT "+COLUMNAS+" FROM cliente WHERE activo=true ORDER BY nombre_razon_social,id_cliente",CLIENTE);
    }

    @Override
    public Optional<Cliente> buscarActivo(long id) {
        return db.query("SELECT "+COLUMNAS+" FROM cliente WHERE id_cliente=? AND activo=true",CLIENTE,id).stream().findFirst();
    }

    @Override
    public Cliente crear(ClienteDatos d) {
        return db.query("INSERT INTO cliente(tipo_cliente,nombre_razon_social,documento,telefono,zona) " +
            "VALUES (?,?,?,?,?) RETURNING "+COLUMNAS,CLIENTE,d.tipoCliente(),d.nombreRazonSocial(),
            d.documento(),d.telefono(),d.zona()).get(0);
    }

    @Override
    public Optional<Cliente> actualizar(long id, ClienteDatos d) {
        return db.query("UPDATE cliente SET tipo_cliente=?,nombre_razon_social=?,documento=?,telefono=?,zona=? " +
            "WHERE id_cliente=? AND activo=true RETURNING "+COLUMNAS,CLIENTE,d.tipoCliente(),
            d.nombreRazonSocial(),d.documento(),d.telefono(),d.zona(),id).stream().findFirst();
    }

    @Override
    public boolean desactivar(long id) {
        return db.update("UPDATE cliente SET activo=false WHERE id_cliente=? AND activo=true",id)==1;
    }

    @Override
    public boolean tienePedidosAbiertos(long id) {
        Integer count=db.queryForObject("SELECT count(*) FROM pedido WHERE id_cliente=? " +
            "AND estado IN ('PENDIENTE','EN_PREPARACION')",Integer.class,id);
        return count!=null && count>0;
    }
}
