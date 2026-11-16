package pe.edu.upeu.moliselva.application.proveedor.usecase;

import pe.edu.upeu.moliselva.application.shared.FallaNegocio;

public class ProveedorNoEncontrado extends FallaNegocio {
    public ProveedorNoEncontrado() { super("El proveedor no existe o está inactivo"); }
    @Override public Tipo tipo() { return Tipo.NO_ENCONTRADO; }
    @Override public String codigo() { return "PROVEEDOR_NO_ENCONTRADO"; }
}
