package pe.edu.upeu.moliselva.presentation.rest.error;

import java.util.Map;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.edu.upeu.moliselva.application.shared.FallaNegocio;

/**
 * Traduce las fallas de los casos de uso al contrato de errores de la API:
 * {@code {"codigo": ..., "mensaje": ...}}. Va antes que el manejador general
 * de {@code comun.ApiErrors}, que conserva su respuesta para errores no previstos.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ManejadorFallasNegocio {

    @ExceptionHandler(FallaNegocio.class)
    public ResponseEntity<Map<String,String>> falla(FallaNegocio ex) {
        return ResponseEntity.status(estado(ex.tipo()))
            .body(Map.of("codigo",ex.codigo(),"mensaje",ex.getMessage()));
    }

    static HttpStatus estado(FallaNegocio.Tipo tipo) {
        return switch (tipo) {
            case NO_AUTENTICADO -> HttpStatus.UNAUTHORIZED;
            case SIN_PERMISO -> HttpStatus.FORBIDDEN;
            case DATOS_INVALIDOS -> HttpStatus.BAD_REQUEST;
            case NO_ENCONTRADO -> HttpStatus.NOT_FOUND;
            case CONFLICTO -> HttpStatus.CONFLICT;
        };
    }
}
