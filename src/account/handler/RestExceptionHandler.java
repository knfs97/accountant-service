package account.handler;

import account.exception.*;
import account.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;

import java.io.IOException;
import java.time.LocalDateTime;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(BadPostRequestException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(BadPostRequestException ex) {
        ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), 400, "Bad Request", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmailTakenException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(EmailTakenException ex) {
        ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), 400, "Bad Request", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(MalformedEmailException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(MalformedEmailException ex) {
        ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), 400, "Bad Request", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(CustomAccessDeniedException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(CustomAccessDeniedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), 403, "Forbidden", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public void handleConstraintViolationException(ConstraintViolationException ex, ServletWebRequest webRequest) throws IOException {
        assert webRequest.getResponse() != null;
        webRequest.getResponse().sendError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ExceptionHandler(UserLockedException.class)
    public ResponseEntity<ErrorResponse> handleUserLockedException(UserLockedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), 401, "Unauthorized", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
}
