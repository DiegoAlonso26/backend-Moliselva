package pe.edu.upeu.moliselva.inventario;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
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

    private String csvText(String value) {
        return "\""+value.replace("\"","\"\"")+"\"";
    }
}
