package ru.serge2nd.octopussy.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import ru.serge2nd.octopussy.support.DataEnvironmentDefinition;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

import static java.lang.invoke.MethodHandles.lookup;
import static org.springframework.web.servlet.function.ServerResponse.status;
import static ru.serge2nd.ObjectAssist.errNotInstantiable;
import static ru.serge2nd.octopussy.api.ErrorInfo.errorCode;
import static ru.serge2nd.stream.FilteringCollectors.filterToList;
import static ru.serge2nd.stream.util.Collecting.collect;

class Cares {
    private Cares() { throw errNotInstantiable(lookup().lookupClass()); }

    static String dataEnvId(ServerRequest rq) { return rq.pathVariable(Router.P_ENV_ID); }

    static URI dataEnvUri(DataEnvironmentDefinition d) { return URI.create(String.format("%s/%s", Router.DATA_ENVS, d.getEnvId())); }

    static ServerResponse handle(ServerRequest sr, HttpStatus status, Throwable e) {
        HttpServletRequest rq = sr.servletRequest();
        return status(status).body(ErrorInfo.builder()
                .url(rq.getRequestURL().toString())
                .method(rq.getMethod())
                .status(status)
                .code(errorCode(e))
                .messages(collect(e.getMessage().split("\n"), filterToList(s -> !s.trim().isEmpty(), 0)))
                .build());
    }
}
