package pe.edu.upeu.moliselva.application.inventario.gateway;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import pe.edu.upeu.moliselva.application.inventario.kardex.MovimientoKardexResponse;

/** Lecturas del kardex de maíz (lado de lectura, SQL directo a la respuesta). */
public interface KardexMaizConsultaGateway {
    Optional<Long> maizId();

    List<MovimientoKardexResponse> movimientos(long idMaiz);

    BigDecimal stock(long idMaiz);
}
