package pe.edu.upeu.moliselva.presentation.rest.maestros.mapper;

import pe.edu.upeu.moliselva.domain.maestros.Cliente;
import pe.edu.upeu.moliselva.domain.maestros.ClienteDatos;
import pe.edu.upeu.moliselva.domain.maestros.Insumo;
import pe.edu.upeu.moliselva.domain.maestros.InsumoDatos;
import pe.edu.upeu.moliselva.domain.maestros.Producto;
import pe.edu.upeu.moliselva.domain.maestros.ProductoDatos;
import pe.edu.upeu.moliselva.presentation.rest.maestros.dto.MaestrosDto;

public final class MaestrosMapper {
    private MaestrosMapper() {}

    public static MaestrosDto.Cliente aRespuesta(Cliente c) {
        return new MaestrosDto.Cliente(c.idCliente(),c.tipoCliente(),c.nombreRazonSocial(),c.documento(),c.telefono(),c.zona());
    }

    public static ClienteDatos aDatos(MaestrosDto.ClienteDatos d) {
        return new ClienteDatos(d.tipoCliente(),d.nombreRazonSocial(),d.documento(),d.telefono(),d.zona());
    }

    public static MaestrosDto.Insumo aRespuesta(Insumo i) {
        return new MaestrosDto.Insumo(i.idInsumo(),i.nombre(),i.unidadMedida());
    }

    public static InsumoDatos aDatos(MaestrosDto.InsumoDatos d) {
        return new InsumoDatos(d.nombre(),d.unidadMedida());
    }

    public static MaestrosDto.Producto aRespuesta(Producto p) {
        return new MaestrosDto.Producto(p.idProducto(),p.nombre(),p.especie(),p.fase(),p.lineaComercial(),p.presentacion());
    }

    public static ProductoDatos aDatos(MaestrosDto.ProductoDatos d) {
        return new ProductoDatos(d.nombre(),d.especie(),d.fase(),d.lineaComercial(),d.presentacion());
    }
}
