package pe.edu.upeu.moliselva.presentation.rest.inventario.mapper;

import pe.edu.upeu.moliselva.domain.inventario.AjusteInventario;
import pe.edu.upeu.moliselva.domain.inventario.ItemStock;
import pe.edu.upeu.moliselva.domain.inventario.MovimientoInventario;
import pe.edu.upeu.moliselva.presentation.rest.inventario.dto.InventarioDto;

public final class InventarioMapper {
    private InventarioMapper() {}

    public static InventarioDto.ItemStock aRespuesta(ItemStock i) {
        return new InventarioDto.ItemStock(i.tipoItem(),i.idItem(),i.nombre(),i.unidadMedida(),i.stockActual());
    }

    public static InventarioDto.Movimiento aRespuesta(MovimientoInventario m) {
        return new InventarioDto.Movimiento(m.idMovimiento(),m.fechaHora(),m.tipoMovimiento(),m.idInsumo(),
            m.idProducto(),m.cantidad(),m.unidadMedida(),m.idRecepcion(),m.idLote(),m.idDespacho(),m.usuario());
    }

    public static AjusteInventario aAjuste(InventarioDto.Ajuste a) {
        return new AjusteInventario(a.tipoMovimiento(),a.idInsumo(),a.idProducto(),a.cantidad(),a.motivo());
    }
}
