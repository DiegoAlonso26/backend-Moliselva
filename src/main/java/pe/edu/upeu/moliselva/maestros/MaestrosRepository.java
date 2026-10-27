package pe.edu.upeu.moliselva.maestros;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class MaestrosRepository {
    private final JdbcTemplate db;
    public MaestrosRepository(JdbcTemplate db) { this.db=db; }

    private static final RowMapper<MaestrosDto.Cliente> CLIENTE = (rs,n) -> new MaestrosDto.Cliente(
        rs.getLong("id_cliente"),rs.getString("tipo_cliente"),rs.getString("nombre_razon_social"),
        rs.getString("documento"),rs.getString("telefono"),rs.getString("zona"));
    private static final String CLIENTE_COLUMNS = "id_cliente,tipo_cliente,nombre_razon_social,documento,telefono,zona";
    public List<MaestrosDto.Cliente> clientes() {
        return db.query("SELECT "+CLIENTE_COLUMNS+" FROM cliente WHERE activo=true ORDER BY nombre_razon_social,id_cliente",CLIENTE);
    }
    public Optional<MaestrosDto.Cliente> cliente(long id) {
        return db.query("SELECT "+CLIENTE_COLUMNS+" FROM cliente WHERE id_cliente=? AND activo=true",CLIENTE,id).stream().findFirst();
    }
    public MaestrosDto.Cliente crearCliente(MaestrosDto.ClienteDatos d) {
        return db.query("INSERT INTO cliente(tipo_cliente,nombre_razon_social,documento,telefono,zona) " +
            "VALUES (?,?,?,?,?) RETURNING "+CLIENTE_COLUMNS,CLIENTE,d.tipoCliente(),d.nombreRazonSocial().trim(),
            optional(d.documento()),optional(d.telefono()),optional(d.zona())).get(0);
    }
    public Optional<MaestrosDto.Cliente> actualizarCliente(long id, MaestrosDto.ClienteDatos d) {
        return db.query("UPDATE cliente SET tipo_cliente=?,nombre_razon_social=?,documento=?,telefono=?,zona=? " +
            "WHERE id_cliente=? AND activo=true RETURNING "+CLIENTE_COLUMNS,CLIENTE,d.tipoCliente(),
            d.nombreRazonSocial().trim(),optional(d.documento()),optional(d.telefono()),optional(d.zona()),id)
            .stream().findFirst();
    }
    public boolean eliminarCliente(long id) {
        return db.update("UPDATE cliente SET activo=false WHERE id_cliente=? AND activo=true",id)==1;
    }
    public boolean clienteConPedidosAbiertos(long id) {
        Integer count=db.queryForObject("SELECT count(*) FROM pedido WHERE id_cliente=? " +
            "AND estado IN ('PENDIENTE','EN_PREPARACION')",Integer.class,id);
        return count!=null && count>0;
    }

    private static final RowMapper<MaestrosDto.Insumo> INSUMO = (rs,n) -> new MaestrosDto.Insumo(
        rs.getLong("id_insumo"),rs.getString("nombre"),rs.getString("unidad_medida"));
    private static final String INSUMO_COLUMNS = "id_insumo,nombre,unidad_medida";
    public List<MaestrosDto.Insumo> insumos() {
        return db.query("SELECT "+INSUMO_COLUMNS+" FROM insumo WHERE activo=true ORDER BY nombre,id_insumo",INSUMO);
    }
    public Optional<MaestrosDto.Insumo> insumo(long id) {
        return db.query("SELECT "+INSUMO_COLUMNS+" FROM insumo WHERE id_insumo=? AND activo=true",INSUMO,id).stream().findFirst();
    }
    public MaestrosDto.Insumo crearInsumo(MaestrosDto.InsumoDatos d) {
        return db.query("INSERT INTO insumo(nombre,unidad_medida) VALUES (?,?) RETURNING "+INSUMO_COLUMNS,
            INSUMO,d.nombre().trim(),unit(d.unidadMedida())).get(0);
    }
    public Optional<MaestrosDto.Insumo> actualizarInsumo(long id, MaestrosDto.InsumoDatos d) {
        return db.query("UPDATE insumo SET nombre=?,unidad_medida=? WHERE id_insumo=? AND activo=true " +
            "RETURNING "+INSUMO_COLUMNS,INSUMO,d.nombre().trim(),unit(d.unidadMedida()),id).stream().findFirst();
    }
    public boolean eliminarInsumo(long id) {
        return db.update("UPDATE insumo SET activo=false WHERE id_insumo=? AND activo=true",id)==1;
    }
    public boolean insumoEnUso(long id) {
        Boolean inUse=db.queryForObject("SELECT " +
            "EXISTS(SELECT 1 FROM formula_detalle fd JOIN formula f ON f.id_formula=fd.id_formula " +
            "WHERE fd.id_insumo=? AND f.vigente=true) OR " +
            "EXISTS(SELECT 1 FROM lote_insumo li JOIN lote l ON l.id_lote=li.id_lote " +
            "WHERE li.id_insumo=? AND l.estado IN ('PLANIFICADO','EN_PROCESO'))",
            Boolean.class,id,id);
        return Boolean.TRUE.equals(inUse);
    }

    private static final RowMapper<MaestrosDto.Producto> PRODUCTO = (rs,n) -> new MaestrosDto.Producto(
        rs.getLong("id_producto"),rs.getString("nombre"),rs.getString("especie"),
        rs.getString("fase"),rs.getString("linea_comercial"),rs.getString("presentacion"));
    private static final String PRODUCTO_COLUMNS = "id_producto,nombre,especie,fase,linea_comercial,presentacion";
    public List<MaestrosDto.Producto> productos() {
        return db.query("SELECT "+PRODUCTO_COLUMNS+" FROM producto WHERE activo=true ORDER BY nombre,id_producto",PRODUCTO);
    }
    public Optional<MaestrosDto.Producto> producto(long id) {
        return db.query("SELECT "+PRODUCTO_COLUMNS+" FROM producto WHERE id_producto=? AND activo=true",PRODUCTO,id).stream().findFirst();
    }
    public MaestrosDto.Producto crearProducto(MaestrosDto.ProductoDatos d) {
        return db.query("INSERT INTO producto(nombre,especie,fase,linea_comercial,presentacion) " +
            "VALUES (?,?,?,?,?) RETURNING "+PRODUCTO_COLUMNS,PRODUCTO,d.nombre().trim(),
            optional(d.especie()),optional(d.fase()),d.lineaComercial(),presentation(d.presentacion())).get(0);
    }
    public Optional<MaestrosDto.Producto> actualizarProducto(long id, MaestrosDto.ProductoDatos d) {
        return db.query("UPDATE producto SET nombre=?,especie=?,fase=?,linea_comercial=?,presentacion=? " +
            "WHERE id_producto=? AND activo=true RETURNING "+PRODUCTO_COLUMNS,PRODUCTO,d.nombre().trim(),
            optional(d.especie()),optional(d.fase()),d.lineaComercial(),presentation(d.presentacion()),id)
            .stream().findFirst();
    }
    public boolean eliminarProducto(long id) {
        return db.update("UPDATE producto SET activo=false WHERE id_producto=? AND activo=true",id)==1;
    }
    public boolean productoEnUso(long id) {
        Boolean inUse=db.queryForObject("SELECT " +
            "EXISTS(SELECT 1 FROM formula WHERE id_producto=? AND vigente=true) OR " +
            "EXISTS(SELECT 1 FROM lote WHERE id_producto=? AND estado IN ('PLANIFICADO','EN_PROCESO')) OR " +
            "EXISTS(SELECT 1 FROM pedido_detalle pd JOIN pedido p ON p.id_pedido=pd.id_pedido " +
            "WHERE pd.id_producto=? AND p.estado IN ('PENDIENTE','EN_PREPARACION')) OR " +
            "EXISTS(SELECT 1 FROM despacho_detalle dd JOIN despacho d ON d.id_despacho=dd.id_despacho " +
            "WHERE dd.id_producto=? AND d.estado='BORRADOR')",Boolean.class,id,id,id,id);
        return Boolean.TRUE.equals(inUse);
    }

    private static String optional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
    private static String unit(String value) { return value==null || value.isBlank() ? "kg" : value.trim(); }
    private static String presentation(String value) {
        return value==null || value.isBlank() ? "Saco de 40 kg" : value.trim();
    }
}
