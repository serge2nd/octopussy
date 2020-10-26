package ru.serge2nd.test.util;

import org.hamcrest.Matcher;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import ru.serge2nd.test.matcher.MatcherOf;

import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT;
import static ru.serge2nd.test.Cfg.EOL;
import static ru.serge2nd.test.matcher.MatchAssist.descriptor;
import static ru.serge2nd.test.matcher.MatchAssist.valueDescriptor;
import static ru.serge2nd.test.util.ToRun.throwSneaky;

public class CustomMatchers {

    @SuppressWarnings("ConstantConditions")
    public static Matcher<String> equalToJson(String expected) {
        ThreadLocal<JSONCompareResult> result = new ThreadLocal<>();
        return new MatcherOf<String>(
                descriptor(()->"JSONs are equal"+EOL),
                actual -> set(result, compareJson(expected, actual, LENIENT)).passed(),
                valueDescriptor($->EOL+"mismatch: "+result.get().getMessage())) {};
    }

    static JSONCompareResult compareJson(String expected, String actual, JSONCompareMode mode) {
        try {
            return JSONCompare.compareJSON(expected, actual, mode);
        } catch (Exception e) {
            throwSneaky(e); return null;
        }
    }

    static <T> T set(ThreadLocal<? super T> c, T e) { c.set(e); return e; }
}
