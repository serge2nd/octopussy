package ru.serge2nd.octopussy.data;

import java.util.List;

public interface NativeQueryAdapter {

    List<?> execute(String query);

    int executeUpdate(List<String> queries);
}
