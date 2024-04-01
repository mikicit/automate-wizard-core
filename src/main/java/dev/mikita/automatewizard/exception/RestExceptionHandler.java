package dev.mikita.automatewizard.exception;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    private static void logException(Exception exception) {
        log.error("Exception caught:", exception);
    }

    private static ErrorInfo errorInfo(HttpServletRequest request, Throwable e) {
        return ErrorInfo.builder().message(e.getMessage()).requestUri(request.getRequestURI()).build();
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorInfo> illegalStateException(HttpServletRequest request, IllegalStateException e) {
        logException(e);
        return new ResponseEntity<>(errorInfo(request, e), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorInfo> authException(HttpServletRequest request, AuthException e) {
        logException(e);
        return new ResponseEntity<>(errorInfo(request, e), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorInfo> expiredJwtException(HttpServletRequest request, ExpiredJwtException e) {
        logException(e);
        return new ResponseEntity<>(errorInfo(request, e), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorInfo> exception(HttpServletRequest request, Exception e) {
        logException(e);
        return new ResponseEntity<>(ErrorInfo.builder().message("Unknown error")
                .requestUri(request.getRequestURI()).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
