package pe.edu.upeu.moliselva.domain.shared;

/** Value object: motivo escrito por un usuario, obligatorio y sin espacios sobrantes. */
public record Motivo(String texto) {
    public Motivo {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El motivo es obligatorio");
        }
        texto = texto.trim();
    }

    public static Motivo de(String texto) { return new Motivo(texto); }
}
