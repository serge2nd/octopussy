package ru.serge2nd.octopussy.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.serge2nd.octopussy.service.ex.DataEnvironmentException;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpStatus.*;
import static ru.serge2nd.octopussy.api.ErrorInfo.errorCode;

@ControllerAdvice
public class ErrorHandling {

    @ExceptionHandler(DataEnvironmentException.NotFound.class)
    ResponseEntity<ErrorInfo> notFound(Exception e, HttpServletRequest rq) {
        return handle(rq, NOT_FOUND, e);
    }

    @ExceptionHandler({
            DataEnvironmentException.class,
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class})
    ResponseEntity<ErrorInfo> badRequest(Exception e, HttpServletRequest rq) {
        return handle(rq, BAD_REQUEST, e);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ErrorInfo> unsupportedMediaType(Exception e, HttpServletRequest rq) {
        return handle(rq, UNSUPPORTED_MEDIA_TYPE, e);
    }

    static ResponseEntity<ErrorInfo> handle(HttpServletRequest rq, HttpStatus status, Exception e) {
        return ResponseEntity.status(status)
            .body(ErrorInfo.builder()
                .url(rq.getRequestURL().toString())
                .method(rq.getMethod())
                .status(status)
                .code(errorCode(e))
                .message(e.getMessage())
                .build());
    }
}
