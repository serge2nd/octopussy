package ru.serge2nd.octopussy.api;

import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.web.util.UriTemplate;
import ru.serge2nd.octopussy.AppContracts;
import ru.serge2nd.octopussy.support.DataKitDefinition;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.StringJoiner;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Collections.singletonMap;
import static org.springframework.web.servlet.function.ServerResponse.status;
import static ru.serge2nd.ObjectAssist.errNotInstantiable;
import static ru.serge2nd.bean.BeanCfg.beanNameByClass;
import static ru.serge2nd.octopussy.AppContracts.KIT_ID;
import static ru.serge2nd.octopussy.api.ErrorInfo.PROP_DELIM;
import static ru.serge2nd.octopussy.api.ErrorInfo.errorCode;
import static ru.serge2nd.stream.CommonCollectors.toStr;
import static ru.serge2nd.stream.FilteringCollectors.filterToList;
import static ru.serge2nd.stream.util.Collecting.collect;

class Serve {
    private Serve() { throw errNotInstantiable(lookup()); }

    static final UriTemplate DATA_KIT_TMPL = new UriTemplate(AppContracts.DATA_KIT_PATH);
    static final String DATA_KIT_DEF = beanNameByClass(DataKitDefinition.class);
    static final String QUERIES_RQ   = beanNameByClass(QueriesRq.class);

    static String dataKitId(ServerRequest rq)     { return rq.pathVariable(KIT_ID); }
    static URI    dataKitUri(DataKitDefinition d) { return DATA_KIT_TMPL.expand(singletonMap(KIT_ID, d.getKitId())); }

    @SneakyThrows
    static DataKitDefinition dataKitDefinition(ServerRequest rq, Validator validator) { return validate(rq.body(DataKitDefinition.class), DATA_KIT_DEF, validator); }
    @SneakyThrows
    static QueriesRq         queriesRq(ServerRequest rq, Validator validator)         { return validate(rq.body(QueriesRq.class), QUERIES_RQ, validator); }

    static <T> T validate(T target, String name, Validator validator) {
        Errors result = new BeanPropertyBindingResult(target, name);
        validator.validate(target, result);

        if (result.hasErrors())
            throw new ServerWebInputException(name + PROP_DELIM +
                collect(result.getAllErrors(), toStr(()->new StringJoiner("\n", "\n", ""), 0)));

        return target;
    }

    static ServerResponse handle(ServerRequest sr, HttpStatus status, Throwable e) {
        HttpServletRequest rq = sr.servletRequest();
        return status(status).body(ErrorInfo.builder()
                .url(rq.getRequestURL().toString())
                .method(rq.getMethod())
                .status(status)
                .code(errorCode(e))
                .messages(collect(e.getMessage().split("\n"), filterToList(StringUtils::hasText, 0)))
                .build());
    }
}
