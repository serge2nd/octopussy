package ru.serge2nd.octopussy.api;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.servlet.function.RequestPredicates.contentType;
import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.*;
import static ru.serge2nd.octopussy.AppContracts.*;
import static ru.serge2nd.octopussy.api.Serve.*;
import static ru.serge2nd.stream.MappingCollectors.mapToList;
import static ru.serge2nd.stream.util.Collecting.collect;

@Configuration
@RequiredArgsConstructor
public class Router {
    private final DataKitService dataKitService;
    private final NativeQueryAdapterProvider provider;
    private final Validator v;

    @Bean
    RouterFunction<ServerResponse> routes() { return route()

        //region Data kits

        .GET(DATA_KITS_PATH,
        rq -> ok().body(collect(dataKitService.getAll(), mapToList(DataKit::getDefinition, 0))))

        .GET(DATA_KIT_PATH,
        rq -> ok().body(dataKitService.get(dataKitId(rq)).getDefinition()))

        .POST(DATA_KITS_PATH, contentType(APPLICATION_JSON),
        rq -> {
            DataKitDefinition created = dataKitService.create(dataKitDefinition(rq, v)).getDefinition();
            return created(dataKitUri(created)).body(created);
        })

        .DELETE(DATA_KIT_PATH,
        rq -> {dataKitService.delete(dataKitId(rq)); return noContent().build();})
        //endregion

        //region Queries

        .POST(DATA_KIT_QUERY_PATH, contentType(APPLICATION_JSON),
        rq -> {
            QueryWithParams q = queriesRq(rq, v).toQueries().get(0);
            return ok().body(queryAdapter(rq).execute(q.getQuery(), q.getParams()));
        })

        .POST(DATA_KIT_UPDATE_PATH, contentType(APPLICATION_JSON),
        rq -> ok().body(queryAdapter(rq).executeUpdate(queriesRq(rq, v).toQueries())))
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

    NativeQueryAdapter queryAdapter(ServerRequest rq) { return provider.getQueryAdapter(dataKitId(rq)); }
}
