package pe.edu.upeu.moliselva.application.inventario.usecase;

import pe.edu.upeu.moliselva.application.shared.Actor;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.shared.Permisos;
import pe.edu.upeu.moliselva.domain.inventario.KardexCsv;

public class ExportarKardexMaiz implements ExportarKardexMaizUseCase {
    private final ConsultarKardexMaizUseCase consultar;
    private final BitacoraGateway bitacora;

    public ExportarKardexMaiz(ConsultarKardexMaizUseCase consultar, BitacoraGateway bitacora) {
        this.consultar=consultar; this.bitacora=bitacora;
    }

    @Override
    public byte[] exportar(Actor actor) {
        Permisos.exigir(actor,"No tiene permiso para exportar el kardex",
            Permisos.ADMINISTRADOR,Permisos.JEFE_PLANTA,Permisos.CONTADORA);
        byte[] csv=KardexCsv.generar(consultar.movimientos());
        bitacora.registrar(actor.id(),"EXPORTAR_KARDEX_MAIZ",null);
        return csv;
    }
}
