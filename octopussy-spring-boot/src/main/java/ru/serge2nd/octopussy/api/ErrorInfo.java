package ru.serge2nd.octopussy.api;

import lombok.Builder;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.serge2nd.octopussy.service.ex.DataEnvironmentException;

import java.util.Map;

import static java.lang.String.format;
import static ru.serge2nd.collection.HardProperties.properties;

@Value
@Builder
public class ErrorInfo {
    String url;
    String method;
    String status;
    String code;
    String message;

    public static class ErrorInfoBuilder {
        // lombok-generated code...

        public ErrorInfoBuilder status(HttpStatus httpStatus) {
            status = format("%d %s", httpStatus.value(), httpStatus.getReasonPhrase());
            return this;
        }
    }

    public static String errorCode(Exception e) { return ERROR_CODES.getOrDefault(e.getClass().getName(), e.getClass().getSimpleName()); }

    static Map<String, String> ERROR_CODES = properties(
            DataEnvironmentException.NotFound.class.getName(), "DATA_ENV_NOT_FOUND",
            DataEnvironmentException.Exists.class.getName()  , "DATA_ENV_EXISTS",
            DataEnvironmentException.Closed.class.getName()  , "DATA_ENV_CLOSED",
            MethodArgumentNotValidException.class.getName()  , "NOT_VALID"
    ).toMap();
}
