package antifraud.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    ResponseEntity<Exception> handleConstraintViolationException(ConstraintViolationException exception) {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
