package ru.serge2nd.octopussy.spi;

import java.util.List;

public interface NativeQueryAdapter {

    List<?> execute(String query);

    int executeUpdate(List<String> queries);
}
