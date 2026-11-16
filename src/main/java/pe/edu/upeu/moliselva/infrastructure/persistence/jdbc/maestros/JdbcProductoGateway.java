package pe.edu.upeu.moliselva.infrastructure.persistence.jdbc.maestros;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.application.maestros.gateway.ProductoGateway;
import pe.edu.upeu.moliselva.domain.maestros.Producto;
import pe.edu.upeu.moliselva.domain.maestros.ProductoDatos;

@Repository
public class JdbcProductoGateway implements ProductoGateway {
    private static final RowMapper<Producto> PRODUCTO = (rs,n) -> new Producto(
        rs.getLong("id_producto"),rs.getString("nombre"),rs.getString("especie"),
        rs.getString("fase"),rs.getString("linea_comercial"),rs.getString("presentacion"));
    private static final String COLUMNAS = "id_producto,nombre,especie,fase,linea_comercial,presentacion";
    private final JdbcTemplate db;

    public JdbcProductoGateway(JdbcTemplate db) { this.db=db; }

    @Override
    public List<Producto> activos() {
        return db.query("SELECT "+COLUMNAS+" FROM producto WHERE activo=true ORDER BY nombre,id_producto",PRODUCTO);
    }

    @Override
    public Optional<Producto> buscarActivo(long id) {
        return db.query("SELECT "+COLUMNAS+" FROM producto WHERE id_producto=? AND activo=true",PRODUCTO,id).stream().findFirst();
    }

    @Override
    public Producto crear(ProductoDatos d) {
        return db.query("INSERT INTO producto(nombre,especie,fase,linea_comercial,presentacion) " +
            "VALUES (?,?,?,?,?) RETURNING "+COLUMNAS,PRODUCTO,d.nombre(),
            d.especie(),d.fase(),d.lineaComercial(),d.presentacion()).get(0);
    }

    @Override
    public Optional<Producto> actualizar(long id, ProductoDatos d) {
        return db.query("UPDATE producto SET nombre=?,especie=?,fase=?,linea_comercial=?,presentacion=? " +
            "WHERE id_producto=? AND activo=true RETURNING "+COLUMNAS,PRODUCTO,d.nombre(),
            d.especie(),d.fase(),d.lineaComercial(),d.presentacion(),id).stream().findFirst();
    }

    @Override
    public boolean desactivar(long id) {
        return db.update("UPDATE producto SET activo=false WHERE id_producto=? AND activo=true",id)==1;
    }

    @Override
    public boolean enUso(long id) {
        Boolean inUse=db.queryForObject("SELECT " +
            "EXISTS(SELECT 1 FROM formula WHERE id_producto=? AND vigente=true) OR " +
            "EXISTS(SELECT 1 FROM lote WHERE id_producto=? AND estado IN ('PLANIFICADO','EN_PROCESO')) OR " +
            "EXISTS(SELECT 1 FROM pedido_detalle pd JOIN pedido p ON p.id_pedido=pd.id_pedido " +
            "WHERE pd.id_producto=? AND p.estado IN ('PENDIENTE','EN_PREPARACION')) OR " +
            "EXISTS(SELECT 1 FROM despacho_detalle dd JOIN despacho d ON d.id_despacho=dd.id_despacho " +
            "WHERE dd.id_producto=? AND d.estado='BORRADOR')",Boolean.class,id,id,id,id);
        return Boolean.TRUE.equals(inUse);
    }
}
