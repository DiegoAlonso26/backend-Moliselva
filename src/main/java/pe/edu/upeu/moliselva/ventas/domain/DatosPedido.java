package pe.edu.upeu.moliselva.ventas.domain;

public record DatosPedido(long idCliente, String observacion) {
    public String observacionNormalizada() {
        return observacion == null || observacion.isBlank() ? null : observacion.trim();
    }
}
