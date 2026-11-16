package pe.edu.upeu.moliselva.application.inventario.usecase;

import java.math.BigDecimal;
import java.util.List;
import pe.edu.upeu.moliselva.application.inventario.gateway.KardexMaizGateway;
import pe.edu.upeu.moliselva.domain.inventario.MovimientoKardex;

public class ConsultarKardexMaiz implements ConsultarKardexMaizUseCase {
    private final KardexMaizGateway kardex;

    public ConsultarKardexMaiz(KardexMaizGateway kardex) { this.kardex=kardex; }

    @Override
    public List<MovimientoKardex> movimientos() { return kardex.movimientos(maizId()); }

    @Override
    public BigDecimal stockKg() { return kardex.stock(maizId()); }

    private long maizId() { return kardex.maizId().orElseThrow(MaizNoConfigurado::new); }
}
