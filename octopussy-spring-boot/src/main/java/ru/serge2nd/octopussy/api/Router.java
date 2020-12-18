package ru.serge2nd.octopussy.api;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import ru.serge2nd.octopussy.service.ex.DataEnvironmentException;
import ru.serge2nd.octopussy.spi.DataEnvironment;
import ru.serge2nd.octopussy.spi.DataEnvironmentService;
import ru.serge2nd.octopussy.spi.NativeQueryAdapter;
import ru.serge2nd.octopussy.spi.NativeQueryAdapterProvider;
import ru.serge2nd.octopussy.support.DataEnvironmentDefinition;
import ru.serge2nd.octopussy.util.QueryWithParams;

import java.util.StringJoiner;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.servlet.function.RequestPredicates.contentType;
import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.*;
import static ru.serge2nd.octopussy.api.ErrorInfo.PROP_DELIM;
import static ru.serge2nd.octopussy.api.Cares.*;
import static ru.serge2nd.stream.CommonCollectors.toStr;
import static ru.serge2nd.stream.MappingCollectors.mapToList;
import static ru.serge2nd.stream.util.Collecting.collect;

@Configuration
@RequiredArgsConstructor
public class Router {
    static final String P_ENV_ID = "envId";
    static final String DATA_ENVS = "/dataEnvironments";
    static final String DATA_ENV_BY_ID = format("/dataEnvironments/{%s}", P_ENV_ID);
    static final String DATA_ENV_QUERY = format("/dataEnvironments/{%s}/query", P_ENV_ID);
    static final String DATA_ENV_UPDATE = format("/dataEnvironments/{%s}/update", P_ENV_ID);

    private final DataEnvironmentService dataEnvService;
    private final NativeQueryAdapterProvider provider;
    private final Validator validator;

    @Bean
    RouterFunction<ServerResponse> routes() { return route()

        //region Data environments

        .GET(DATA_ENVS,
        rq -> ok().body(collect(dataEnvService.getAll(), mapToList(DataEnvironment::getDefinition, 0))))

        .GET(DATA_ENV_BY_ID,
        rq -> ok().body(dataEnvService.get(dataEnvId(rq)).getDefinition()))

        .POST(DATA_ENVS, contentType(APPLICATION_JSON),
        rq -> {
            DataEnvironmentDefinition created = dataEnvService.create(validate(rq.body(DataEnvironmentDefinition.class), "dataEnvDefinition")).getDefinition();
            return created(dataEnvUri(created)).body(created);
        })

        .DELETE(DATA_ENV_BY_ID,
        rq -> {dataEnvService.delete(dataEnvId(rq)); return noContent().build();})
        //endregion

        //region Queries

        .POST(DATA_ENV_QUERY, contentType(APPLICATION_JSON),
        rq -> {
            QueryWithParams q = validate(rq.body(QueriesRq.class), "queries").toQueries().get(0);
            return ok().body(queryAdapter(rq).execute(q.getQuery(), q.getParams()));
        })

        .POST(DATA_ENV_UPDATE, contentType(APPLICATION_JSON),
        rq -> ok().body(queryAdapter(rq).executeUpdate(validate(rq.body(QueriesRq.class), "queries").toQueries())))
        //endregion

        //region Error handling

        .onError(DataEnvironmentException.NotFound.class,
        (e, rq) -> handle(rq, NOT_FOUND, e))

        .onError(HttpMediaTypeNotSupportedException.class,
        (e, rq) -> handle(rq, UNSUPPORTED_MEDIA_TYPE, e))

        .onError(e -> e instanceof DataEnvironmentException ||
                      e instanceof ServerWebInputException ||
                      e instanceof HttpMessageNotReadableException,
        (e, rq) -> handle(rq, BAD_REQUEST, e))
        //endregion

        .build();
    }

    <T> T validate(T target, String name) {
        Errors result = new BeanPropertyBindingResult(target, name);
        validator.validate(target, result);
        if (result.hasErrors())
            throw new ServerWebInputException(name + PROP_DELIM +
                collect(result.getAllErrors(), toStr(()->new StringJoiner("\n", "\n", ""), 0)));
        return target;
    }

    NativeQueryAdapter queryAdapter(ServerRequest rq) { return provider.getQueryAdapter(dataEnvId(rq)); }
}
