package pe.edu.upeu.moliselva.inventario;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.moliselva.comun.ApiException;
import pe.edu.upeu.moliselva.comun.BitacoraService;

@Service
public class InventarioService {
    private final InventarioRepository repository;
    private final BitacoraService audit;
    public InventarioService(InventarioRepository repository, BitacoraService audit) {
        this.repository=repository; this.audit=audit;
    }

    public long maizId() {
        return repository.maizId().orElseThrow(() ->
            new ApiException(HttpStatus.CONFLICT,"MAIZ_NO_CONFIGURADO","No existe el insumo Maíz activo"));
    }

    public void registrarEntrada(long idInsumo, long idRecepcion, BigDecimal cantidad, long idUsuario) {
        repository.insertarEntradaMaiz(idInsumo,idRecepcion,cantidad,idUsuario);
    }

    @Transactional(readOnly=true)
    public List<InventarioDto.Kardex> kardex() { return repository.kardexMaiz(maizId()); }

    @Transactional(readOnly=true)
    public InventarioDto.Stock stock() { return new InventarioDto.Stock(repository.stockMaiz(maizId())); }
    @Transactional(readOnly=true)
    public List<InventarioDto.ItemStock> stockTodos() { return repository.stockTodos(); }
    @Transactional(readOnly=true)
    public InventarioDto.ItemStock stockItem(String tipo, long id) {
        if (!tipo.equals("INSUMO") && !tipo.equals("PRODUCTO")) {
            throw new ApiException(HttpStatus.BAD_REQUEST,"TIPO_ITEM_INVALIDO","Use INSUMO o PRODUCTO");
        }
        return repository.stockItem(tipo,id).orElseThrow(() ->
            new ApiException(HttpStatus.NOT_FOUND,"ITEM_NO_ENCONTRADO","El item no existe"));
    }

    @Transactional
    public byte[] exportarCsv(long usuarioId) {
        StringBuilder csv = new StringBuilder("\uFEFFidMovimiento;fechaHora;tipoMovimiento;cantidadKg;saldoKg;idRecepcion;usuario\r\n");
        for (var row : kardex()) {
            csv.append(row.idMovimiento()).append(';').append(row.fechaHora()).append(';')
                .append(row.tipoMovimiento()).append(';').append(row.cantidadKg().toPlainString()).append(';')
                .append(row.saldoKg().toPlainString()).append(';')
                .append(row.idRecepcion()==null ? "" : row.idRecepcion()).append(';')
                .append(csvText(row.usuario())).append("\r\n");
        }
        audit.registrar(usuarioId,"EXPORTAR_KARDEX_MAIZ",null);
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly=true)
    public List<InventarioDto.Movimiento> movimientos() { return repository.movimientos(); }
    @Transactional(readOnly=true)
    public InventarioDto.Movimiento movimiento(long id) {
        return repository.movimiento(id).orElseThrow(() ->
            new ApiException(HttpStatus.NOT_FOUND,"MOVIMIENTO_NO_ENCONTRADO","El movimiento no existe"));
    }
    @Transactional
    public InventarioDto.Movimiento ajustar(InventarioDto.Ajuste input, long userId) {
        if ((input.idInsumo()==null) == (input.idProducto()==null) ||
                (input.idInsumo()!=null && input.idInsumo()<=0) ||
                (input.idProducto()!=null && input.idProducto()<=0)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,"ITEM_INVALIDO",
                "Envíe un idInsumo o un idProducto válido, pero no ambos");
        }
        if (!repository.itemActivo(input.idInsumo(),input.idProducto())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,"ITEM_INVALIDO","El insumo o producto no existe o está inactivo");
        }
        long id;
        try {
            id=repository.insertarAjuste(input,repository.unidadMedida(input.idInsumo()),userId);
        } catch (DataAccessException ex) {
            if (ex.getMessage()!=null && ex.getMessage().contains("Stock insuficiente")) {
                throw new ApiException(HttpStatus.CONFLICT,"STOCK_INSUFICIENTE","El ajuste dejaría stock negativo");
            }
            throw ex;
        }
        audit.registrar(userId,"AJUSTE_INVENTARIO id="+id,input.motivo().trim());
        return movimiento(id);
    }

    private String csvText(String value) {
        return "\""+value.replace("\"","\"\"")+"\"";
    }
}
