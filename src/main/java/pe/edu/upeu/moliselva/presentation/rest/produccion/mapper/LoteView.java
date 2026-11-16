package pe.edu.upeu.moliselva.presentation.rest.produccion.mapper;

import java.util.LinkedHashMap;
import java.util.Map;
import pe.edu.upeu.moliselva.domain.produccion.Lote;
import pe.edu.upeu.moliselva.domain.produccion.LoteInsumo;

public final class LoteView {
    private LoteView() {}
    public static Map<String,Object> from(Lote lote) {
        Map<String,Object> out=new LinkedHashMap<>();
        out.put("idLote",lote.idLote());
        out.put("idProducto",lote.idProducto());
        out.put("idFormula",lote.idFormula());
        out.put("fechaHora",lote.fechaHora());
        out.put("tamanoKg",lote.tamanoKg());
        out.put("estado",lote.estado().name());
        out.put("idResponsable",lote.idResponsable());
        return out;
    }
    public static Map<String,Object> from(LoteInsumo insumo) {
        Map<String,Object> out=new LinkedHashMap<>();
        out.put("idLote",insumo.idLote());
        out.put("idInsumo",insumo.idInsumo());
        out.put("cantidadCalculadaKg",insumo.cantidadCalculadaKg());
        out.put("cantidadRealKg",insumo.cantidadRealKg());
        return out;
    }
}
