package pe.edu.upeu.moliselva.recepcion.adapter.out.jdbc;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.moliselva.inventario.InventarioRepository;
import pe.edu.upeu.moliselva.recepcion.application.port.out.InventarioPort;

@Repository
public class JdbcInventarioAdapter implements InventarioPort {
    private final InventarioRepository inventario;
    public JdbcInventarioAdapter(InventarioRepository inventario) { this.inventario=inventario; }
    @Override
    public Optional<Long> maizId() { return inventario.maizId(); }
    @Override
    public void registrarEntrada(long idInsumo, long idRecepcion,
            BigDecimal cantidad, long idUsuario) {
        inventario.insertarEntradaMaiz(idInsumo,idRecepcion,cantidad,idUsuario);
    }
}
