package enviosrutasdelinca.shared.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorErrores.class);

    public record ErrorRespuesta(int status, String mensaje, Map<String, String> errores) {
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorRespuesta> manejarEstado(ResponseStatusException error) {
        String mensaje = error.getReason() == null ? "La solicitud no es válida." : error.getReason();
        return responder(error.getStatusCode().value(), mensaje, Map.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorRespuesta> manejarSolicitudInvalida(IllegalArgumentException error) {
        return responder(HttpStatus.BAD_REQUEST.value(), error.getMessage(), Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> manejarValidacion(MethodArgumentNotValidException error) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError campo : error.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(campo.getField(), campo.getDefaultMessage());
        }
        FieldError primero = error.getBindingResult().getFieldError();
        String mensaje = primero == null ? "La solicitud no es válida." : primero.getDefaultMessage();
        return responder(HttpStatus.BAD_REQUEST.value(), mensaje, errores);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> manejarCuerpoInvalido(HttpMessageNotReadableException error) {
        return responder(HttpStatus.BAD_REQUEST.value(),
                "Los datos enviados no tienen el formato correcto.", Map.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRespuesta> manejarTipoInvalido(MethodArgumentTypeMismatchException error) {
        return responder(HttpStatus.BAD_REQUEST.value(),
                "El valor de «" + error.getName() + "» no es válido.", Map.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorRespuesta> manejarParametroFaltante(MissingServletRequestParameterException error) {
        return responder(HttpStatus.BAD_REQUEST.value(),
                "Falta el dato «" + error.getParameterName() + "».", Map.of());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorRespuesta> manejarDuplicado(DuplicateKeyException error) {
        return responder(HttpStatus.CONFLICT.value(), "Ya existe un registro con esos datos.", Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuesta> manejarIntegridad(DataIntegrityViolationException error) {
        LOG.warn("Regla de la base de datos rechazó la operación", error);
        return responder(HttpStatus.CONFLICT.value(),
                "La operación entra en conflicto con otros datos existentes.", Map.of());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorRespuesta> manejarBaseDeDatos(DataAccessException error) {
        LOG.error("Error de acceso a datos", error);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocurrió un error al acceder a los datos. Inténtalo nuevamente.", Map.of());
    }

    private ResponseEntity<ErrorRespuesta> responder(int status, String mensaje, Map<String, String> errores) {
        return ResponseEntity.status(status).body(new ErrorRespuesta(status, mensaje, errores));
    }
}