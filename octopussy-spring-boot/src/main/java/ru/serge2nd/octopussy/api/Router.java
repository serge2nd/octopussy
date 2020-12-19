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
import ru.serge2nd.octopussy.service.ex.DataKitException;
import ru.serge2nd.octopussy.spi.DataKit;
import ru.serge2nd.octopussy.spi.DataKitService;
import ru.serge2nd.octopussy.spi.NativeQueryAdapter;
import ru.serge2nd.octopussy.spi.NativeQueryAdapterProvider;
import ru.serge2nd.octopussy.support.DataKitDefinition;
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
    static final String P_KIT_ID = "kitId";
    static final String DATA_KITS = "/dataKits";
    static final String DATA_KIT_BY_ID  = format("%s/{%s}", DATA_KITS, P_KIT_ID);
    static final String DATA_KIT_QUERY  = format("%s/{%s}/query", DATA_KITS, P_KIT_ID);
    static final String DATA_KIT_UPDATE = format("%s/{%s}/update", DATA_KITS, P_KIT_ID);

    private final DataKitService dataKitService;
    private final NativeQueryAdapterProvider provider;
    private final Validator validator;

    @Bean
    RouterFunction<ServerResponse> routes() { return route()

        //region Data kits

        .GET(DATA_KITS,
        rq -> ok().body(collect(dataKitService.getAll(), mapToList(DataKit::getDefinition, 0))))

        .GET(DATA_KIT_BY_ID,
        rq -> ok().body(dataKitService.get(dataKitId(rq)).getDefinition()))

        .POST(DATA_KITS, contentType(APPLICATION_JSON),
        rq -> {
            DataKitDefinition created = dataKitService.create(validate(rq.body(DataKitDefinition.class), "dataKitDefinition")).getDefinition();
            return created(dataKitUri(created)).body(created);
        })

        .DELETE(DATA_KIT_BY_ID,
        rq -> {
            dataKitService.delete(dataKitId(rq)); return noContent().build();})
        //endregion

        //region Queries

        .POST(DATA_KIT_QUERY, contentType(APPLICATION_JSON),
        rq -> {
            QueryWithParams q = validate(rq.body(QueriesRq.class), "queries").toQueries().get(0);
            return ok().body(queryAdapter(rq).execute(q.getQuery(), q.getParams()));
        })

        .POST(DATA_KIT_UPDATE, contentType(APPLICATION_JSON),
        rq -> ok().body(queryAdapter(rq).executeUpdate(validate(rq.body(QueriesRq.class), "queries").toQueries())))
        //endregion

        //region Error handling

        .onError(DataKitException.NotFound.class,
        (e, rq) -> handle(rq, NOT_FOUND, e))

        .onError(HttpMediaTypeNotSupportedException.class,
        (e, rq) -> handle(rq, UNSUPPORTED_MEDIA_TYPE, e))

        .onError(e -> e instanceof DataKitException ||
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

    NativeQueryAdapter queryAdapter(ServerRequest rq) { return provider.getQueryAdapter(dataKitId(rq)); }
}
