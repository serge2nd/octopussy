package ru.serge2nd.octopussy.spi;

import ru.serge2nd.octopussy.util.Queries;

import java.util.List;
import java.util.Map;

/**
 * An adapter for convenient transactional execution of SQL statements.
 * @see javax.persistence.EntityManager#createNativeQuery(String)
 */
public interface NativeQueryAdapter {

    List<?> execute(String query, Map<String, Object> params);

    int[]   executeUpdate(Queries queries);
}
