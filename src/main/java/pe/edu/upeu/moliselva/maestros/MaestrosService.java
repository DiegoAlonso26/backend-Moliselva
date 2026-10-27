package pe.edu.upeu.moliselva.maestros;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.moliselva.comun.ApiException;
import pe.edu.upeu.moliselva.comun.BitacoraService;
import pe.edu.upeu.moliselva.inventario.InventarioRepository;

@Service
public class MaestrosService {
    private final MaestrosRepository repository;
    private final BitacoraService audit;
    private final InventarioRepository inventory;
    public MaestrosService(MaestrosRepository repository, BitacoraService audit,
            InventarioRepository inventory) {
        this.repository=repository; this.audit=audit; this.inventory=inventory;
    }

    public List<MaestrosDto.Cliente> clientes() { return repository.clientes(); }
    public MaestrosDto.Cliente cliente(long id) {
        return repository.cliente(id).orElseThrow(() -> missing("CLIENTE"));
    }
    @Transactional
    public MaestrosDto.Cliente crearCliente(MaestrosDto.ClienteDatos input, long userId) {
        var row=repository.crearCliente(input);
        audit.registrar(userId,"CREAR_CLIENTE id="+row.idCliente(),null);
        return row;
    }
    @Transactional
    public MaestrosDto.Cliente actualizarCliente(long id, MaestrosDto.ClienteDatos input, long userId) {
        var row=repository.actualizarCliente(id,input).orElseThrow(() -> missing("CLIENTE"));
        audit.registrar(userId,"ACTUALIZAR_CLIENTE id="+id,null);
        return row;
    }
    @Transactional
    public void eliminarCliente(long id, long userId) {
        if (repository.clienteConPedidosAbiertos(id)) {
            throw new ApiException(HttpStatus.CONFLICT,"CLIENTE_CON_PEDIDOS",
                "El cliente tiene pedidos abiertos");
        }
        if (!repository.eliminarCliente(id)) { throw missing("CLIENTE"); }
        audit.registrar(userId,"DESACTIVAR_CLIENTE id="+id,null);
    }

    public List<MaestrosDto.Insumo> insumos() { return repository.insumos(); }
    public MaestrosDto.Insumo insumo(long id) {
        return repository.insumo(id).orElseThrow(() -> missing("INSUMO"));
    }
    @Transactional
    public MaestrosDto.Insumo crearInsumo(MaestrosDto.InsumoDatos input, long userId) {
        var row=repository.crearInsumo(input);
        audit.registrar(userId,"CREAR_INSUMO id="+row.idInsumo(),null);
        return row;
    }
    @Transactional
    public MaestrosDto.Insumo actualizarInsumo(long id, MaestrosDto.InsumoDatos input, long userId) {
        protectMaiz(id);
        var row=repository.actualizarInsumo(id,input).orElseThrow(() -> missing("INSUMO"));
        audit.registrar(userId,"ACTUALIZAR_INSUMO id="+id,null);
        return row;
    }
    @Transactional
    public void eliminarInsumo(long id, long userId) {
        protectMaiz(id);
        stockCero("INSUMO",id);
        if (repository.insumoEnUso(id)) {
            throw new ApiException(HttpStatus.CONFLICT,"INSUMO_EN_USO",
                "El insumo figura en una fórmula vigente o un lote activo");
        }
        if (!repository.eliminarInsumo(id)) { throw missing("INSUMO"); }
        audit.registrar(userId,"DESACTIVAR_INSUMO id="+id,null);
    }

    public List<MaestrosDto.Producto> productos() { return repository.productos(); }
    public MaestrosDto.Producto producto(long id) {
        return repository.producto(id).orElseThrow(() -> missing("PRODUCTO"));
    }
    @Transactional
    public MaestrosDto.Producto crearProducto(MaestrosDto.ProductoDatos input, long userId) {
        var row=repository.crearProducto(input);
        audit.registrar(userId,"CREAR_PRODUCTO id="+row.idProducto(),null);
        return row;
    }
    @Transactional
    public MaestrosDto.Producto actualizarProducto(long id, MaestrosDto.ProductoDatos input, long userId) {
        var row=repository.actualizarProducto(id,input).orElseThrow(() -> missing("PRODUCTO"));
        audit.registrar(userId,"ACTUALIZAR_PRODUCTO id="+id,null);
        return row;
    }
    @Transactional
    public void eliminarProducto(long id, long userId) {
        stockCero("PRODUCTO",id);
        if (repository.productoEnUso(id)) {
            throw new ApiException(HttpStatus.CONFLICT,"PRODUCTO_EN_USO",
                "El producto figura en una fórmula, lote, pedido o despacho activo");
        }
        if (!repository.eliminarProducto(id)) { throw missing("PRODUCTO"); }
        audit.registrar(userId,"DESACTIVAR_PRODUCTO id="+id,null);
    }

    private void protectMaiz(long id) {
        if (repository.insumo(id).map(row -> row.nombre().equals("Maíz")).orElse(false)) {
            throw new ApiException(HttpStatus.CONFLICT,"INSUMO_SISTEMA",
                "El insumo Maíz sostiene el kardex de la demo y no se puede modificar ni desactivar");
        }
    }
    private void stockCero(String tipo, long id) {
        if (inventory.stockItem(tipo,id).map(row -> row.stockActual().signum()!=0).orElse(false)) {
            throw new ApiException(HttpStatus.CONFLICT,"ITEM_CON_STOCK",
                "El insumo o producto tiene stock; registre su salida antes de desactivarlo");
        }
    }
    private ApiException missing(String entity) {
        return new ApiException(HttpStatus.NOT_FOUND,entity+"_NO_ENCONTRADO",
            "El registro no existe o está inactivo");
    }
}
