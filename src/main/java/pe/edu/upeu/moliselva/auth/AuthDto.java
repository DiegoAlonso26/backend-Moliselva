package pe.edu.upeu.moliselva.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDto {
    private AuthDto() {}
    public record LoginRequest(
        @Schema(example="operario_demo") @NotBlank String usuario,
        @Schema(description="Valor de DEMO_PASSWORD en el archivo .env local", example="tu-clave-local")
        @NotBlank String password) {}
    public record Usuario(long id, String nombres, String usuario, String rol, boolean activo) {}
    public record LoginResponse(String token, Usuario usuario) {}
    public record NuevoUsuario(
        @NotBlank @Size(max=120) String nombres,
        @NotBlank @Size(max=60) String usuario,
        @NotBlank @Size(min=12) String password,
        @NotBlank @Pattern(regexp="ADMINISTRADOR|JEFE_PLANTA|OPERARIO|CONTADORA") String rol) {}
    public record ActualizarUsuario(
        @NotBlank @Size(max=120) String nombres,
        @NotBlank @Size(max=60) String usuario,
        @Size(min=12) String password,
        @NotBlank @Pattern(regexp="ADMINISTRADOR|JEFE_PLANTA|OPERARIO|CONTADORA") String rol) {}
}
