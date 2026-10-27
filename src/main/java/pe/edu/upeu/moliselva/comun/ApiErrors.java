package pe.edu.upeu.moliselva.comun;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import pe.edu.upeu.moliselva.recepcion.application.RecepcionFailure;

@RestControllerAdvice
public class ApiErrors {
    private static final Logger log = LoggerFactory.getLogger(ApiErrors.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String,String>> known(ApiException ex) {
        return ResponseEntity.status(ex.status()).body(Map.of("codigo",ex.codigo(),"mensaje",ex.getMessage()));
    }

    @ExceptionHandler(RecepcionFailure.class)
    public ResponseEntity<Map<String,String>> reception(RecepcionFailure ex) {
        HttpStatus status = switch (ex.reason()) {
            case SIN_PERMISO -> HttpStatus.FORBIDDEN;
            case PROVEEDOR_INVALIDO, PESOS_INVALIDOS, HUMEDAD_FUERA_DE_RANGO -> HttpStatus.BAD_REQUEST;
            case RECEPCION_NO_ENCONTRADA -> HttpStatus.NOT_FOUND;
            case MAIZ_NO_CONFIGURADO, RECEPCION_NO_PENDIENTE, RECEPCION_NO_EDITABLE -> HttpStatus.CONFLICT;
        };
        return ResponseEntity.status(status)
            .body(Map.of("codigo",ex.reason().name(),"mensaje",ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> invalid(MethodArgumentNotValidException ex) {
        boolean humidity = ex.getBindingResult().getFieldErrors().stream()
            .anyMatch(field -> field.getField().equals("humedadPct"));
        String codigo = humidity ? "HUMEDAD_FUERA_DE_RANGO" : "DATOS_INVALIDOS";
        String mensaje = humidity ? "La humedad debe estar entre 0 y 100" : "Revise los campos enviados";
        return ResponseEntity.badRequest().body(Map.of("codigo",codigo,"mensaje",mensaje));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String,String>> unreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(Map.of("codigo","JSON_INVALIDO","mensaje","El cuerpo JSON no es válido"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String,String>> conflict(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("codigo","CONFLICTO_DATOS","mensaje","La operación contradice una regla de la base de datos"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String,String>> denied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("codigo","SIN_PERMISO","mensaje","No tiene permiso para esta acción"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String,String>> typeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "fecha".equals(ex.getName())
            ? "La fecha debe tener el formato AAAA-MM-DD" : "Revise los parámetros enviados";
        return ResponseEntity.badRequest().body(Map.of("codigo","DATOS_INVALIDOS","mensaje",message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,String>> unexpected(Exception ex) {
        log.error("Error no previsto al procesar la solicitud", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("codigo","ERROR_INTERNO","mensaje","Ocurrió un error interno"));
    }
}
