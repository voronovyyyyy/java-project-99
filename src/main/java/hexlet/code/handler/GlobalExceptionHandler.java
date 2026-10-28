package hexlet.code.handler;

import hexlet.code.exception.BadRequestException;
import hexlet.code.exception.ResourceNotFoundException;
import io.sentry.Sentry;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record ApiError(int status, String message) {}

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> notFound(ResourceNotFoundException error) {
        return error(HttpStatus.NOT_FOUND, error.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> unauthorized(AuthenticationException error) {
        return error(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> forbidden(AccessDeniedException error) {
        return error(HttpStatus.FORBIDDEN, "У вас нет прав на выполнение этой операции");
    }

    @ExceptionHandler({
        BadRequestException.class,
        IllegalArgumentException.class,
        ConstraintViolationException.class,
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiError> badRequest(Exception error) {
        return error(HttpStatus.BAD_REQUEST, "Некорректные данные запроса");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException error) {
        var messages =
                error.getBindingResult().getFieldErrors().stream()
                        .map(field -> field.getField() + ": " + field.getDefaultMessage())
                        .distinct()
                        .sorted()
                        .toList();
        return error(HttpStatus.BAD_REQUEST, String.join("; ", messages));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> conflict(DataIntegrityViolationException error) {
        return error(HttpStatus.BAD_REQUEST, "Нарушены ограничения уникальности или связей данных");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception exception) {
        if (exception instanceof ErrorResponse response
                && !response.getStatusCode().is5xxServerError()) {
            return ResponseEntity.status(response.getStatusCode())
                    .body(
                            new ApiError(
                                    response.getStatusCode().value(),
                                    response.getBody().getTitle()));
        }
        LOG.error("Unexpected request failure", exception);
        Sentry.captureException(exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiError(status.value(), message));
    }
}
