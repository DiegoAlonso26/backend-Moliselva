package pe.edu.upeu.moliselva.application.inventario.kardex;

import pe.edu.upeu.moliselva.application.shared.FallaNegocio;

public class MaizNoConfigurado extends FallaNegocio {
    public MaizNoConfigurado() { super("No existe el insumo Maíz activo"); }
    @Override public Tipo tipo() { return Tipo.CONFLICTO; }
    @Override public String codigo() { return "MAIZ_NO_CONFIGURADO"; }
}
