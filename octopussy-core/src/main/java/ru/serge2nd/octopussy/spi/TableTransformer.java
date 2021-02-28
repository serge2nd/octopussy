package ru.serge2nd.octopussy.spi;

import java.util.List;

/** A transformer of a table (list of tuples). */
public interface TableTransformer {

    Object transform(Object[] tuple, String[] aliases);

    List<?> transform(List<?> list);
}
