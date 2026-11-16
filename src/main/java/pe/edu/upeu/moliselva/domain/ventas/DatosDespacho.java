package pe.edu.upeu.moliselva.domain.ventas;

public record DatosDespacho(long idPedido, TipoEntrega tipoEntrega, String destino) {
    public String destinoNormalizado() {
        return destino == null || destino.isBlank() ? null : destino.trim();
    }
}
