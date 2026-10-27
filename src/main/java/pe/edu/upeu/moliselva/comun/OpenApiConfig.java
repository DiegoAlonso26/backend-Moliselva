package pe.edu.upeu.moliselva.comun;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
    title = "MoliSelva - API PO-01",
    version = "0.1.0",
    description = "Recepcion de maiz, inventario y administracion de usuarios. "
        + "Ejecuta POST /api/auth/login y copia el token en Authorize para probar las rutas protegidas."))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP,
    scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {}
