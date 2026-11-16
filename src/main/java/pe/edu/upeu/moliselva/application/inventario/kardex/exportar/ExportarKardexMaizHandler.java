package pe.edu.upeu.moliselva.application.inventario.kardex.exportar;

import pe.edu.upeu.moliselva.application.inventario.gateway.KardexMaizConsultaGateway;
import pe.edu.upeu.moliselva.application.inventario.kardex.MaizNoConfigurado;
import pe.edu.upeu.moliselva.application.shared.BitacoraGateway;
import pe.edu.upeu.moliselva.application.shared.Permisos;
import pe.edu.upeu.moliselva.application.shared.cqrs.CommandHandler;

public class ExportarKardexMaizHandler implements CommandHandler<ExportarKardexMaizCommand, ExportarKardexMaizResponse> {
    private final KardexMaizConsultaGateway kardex;
    private final BitacoraGateway bitacora;

    public ExportarKardexMaizHandler(KardexMaizConsultaGateway kardex, BitacoraGateway bitacora) {
        this.kardex=kardex; this.bitacora=bitacora;
    }

    @Override
    public ExportarKardexMaizResponse handle(ExportarKardexMaizCommand command) {
        var actor=command.actor();
        Permisos.exigir(actor,"No tiene permiso para exportar el kardex",
            Permisos.ADMINISTRADOR,Permisos.JEFE_PLANTA,Permisos.CONTADORA);
        byte[] csv=KardexCsv.generar(kardex.movimientos(kardex.maizId().orElseThrow(MaizNoConfigurado::new)));
        bitacora.registrar(actor.id(),"EXPORTAR_KARDEX_MAIZ",null);
        return new ExportarKardexMaizResponse(csv);
    }
}
