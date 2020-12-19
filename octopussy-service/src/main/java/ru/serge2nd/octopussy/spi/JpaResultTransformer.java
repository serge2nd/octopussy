package ru.serge2nd.octopussy.spi;

import java.util.List;

/** JPA query result transformer. */
public interface JpaResultTransformer {

    Object transform(Object[] tuple, String[] aliases);

    List<?> transform(List<?> list);
}
