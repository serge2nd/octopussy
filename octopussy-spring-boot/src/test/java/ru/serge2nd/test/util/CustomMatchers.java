package ru.serge2nd.test.util;

import org.hamcrest.Matcher;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import ru.serge2nd.test.matcher.builder.MatcherBuilder;

import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT;
import static ru.serge2nd.test.Cfg.EOL;
import static ru.serge2nd.test.util.ToRun.throwSneaky;

public class CustomMatchers {

    public static Matcher<String> equalToJson(String expected) {
        return new MatcherBuilder<String>(){}
                .then(json -> compareJson(expected, json, LENIENT))
                .matchIf(JSONCompareResult::passed)
                .append("JSONs are equal" + EOL)
                .alert(r -> EOL + "mismatch: " + r.getMessage())
                .build();
    }

    static JSONCompareResult compareJson(String expected, String actual, JSONCompareMode mode) {
        try {
            return JSONCompare.compareJSON(expected, actual, mode);
        } catch (Exception e) {
            throwSneaky(e); return null;
        }
    }
}
