package ru.serge2nd.octopussy.api;

import lombok.Builder;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebInputException;
import ru.serge2nd.octopussy.service.ex.DataEnvironmentException;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.String.format;
import static ru.serge2nd.stream.CommonCollectors.toList;
import static ru.serge2nd.stream.util.Collecting.collect;
import static ru.serge2nd.stream.util.CollectingOptions.UNMODIFIABLE;

@Value
@Builder
public class ErrorInfo {
    public static final String PROP_DELIM = ":";

    String url, method, status, code;
    List<String> messages;

    public static class ErrorInfoBuilder {
        // lombok-generated code...

        public ErrorInfoBuilder status(HttpStatus httpStatus) {
            status = format("%d %s", httpStatus.value(), httpStatus.getReasonPhrase());
            return this;
        }
    }

    public static String errorCode(Throwable e) {
        for (Entry<Predicate<Throwable>, Function<Throwable, String>> entry : ERROR_CODES)
            if (entry.getKey().test(e))
                return entry.getValue().apply(e);
        return e.getClass().getSimpleName();
    }

    @SuppressWarnings("ConstantConditions")
    static final List<Entry<Predicate<Throwable>, Function<Throwable, String>>> ERROR_CODES = collect(toList(UNMODIFIABLE),
        errorCode(instanceOf(DataEnvironmentException.NotFound.class), e -> errorCode("DATA_ENV_NOT_FOUND", e.getEnvId())),
        errorCode(instanceOf(DataEnvironmentException.Exists.class)  , e -> errorCode("DATA_ENV_EXISTS", e.getEnvId())),
        errorCode(instanceOf(DataEnvironmentException.Closed.class)  , e -> errorCode("DATA_ENV_CLOSED", e.getEnvId())),
        errorCode(instanceOf(ServerWebInputException.class)          , e -> errorCode("NOT_VALID", e.getReason().substring(0, e.getReason().indexOf(PROP_DELIM))))
    );

    @SuppressWarnings("unchecked,rawtypes")
    static <T extends Throwable> Entry<Predicate<Throwable>, Function<Throwable, String>> errorCode(Predicate<T> predicate, Function<T, String> codeProvider) {
        return new SimpleEntry(predicate, codeProvider);
    }

    static String errorCode(String... parts) { return String.join(PROP_DELIM, parts); }

    static <T> Predicate<T> instanceOf(Class<T> cls) { return cls::isInstance; }
}
