package ru.serge2nd.octopussy.spi;

import java.util.List;

public interface ResultTransformer {

    Object transform(Object[] tuple, String[] aliases);

    List<?> transform(List<?> list);
}
