package ru.serge2nd.octopussy.support;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.serge2nd.octopussy.spi.DataEnvironment;
import ru.serge2nd.octopussy.service.ex.DataEnvironmentClosedException;
import ru.serge2nd.octopussy.spi.NativeQueryAdapter;

import javax.persistence.EntityManager;
import java.sql.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static javax.persistence.SynchronizationType.SYNCHRONIZED;

@RequiredArgsConstructor
public class NativeQueryAdapterImpl implements NativeQueryAdapter {
    private final DataEnvironment dataEnv;

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @SuppressWarnings("unchecked")
    public List<?> execute(String query) {
        return execute(em -> (List<?>) em
                .createNativeQuery(query)
                .getResultList().stream()
                .map(NativeQueryAdapterImpl::extractRow)
                .collect(toList()));
    }

    @Transactional
    public int executeUpdate(List<String> queries) {
        return execute(em -> queries.stream()
                .mapToInt(query -> em.createNativeQuery(query).executeUpdate())
                .sum());
    }

    private <R> R execute(Function<EntityManager, R> op) {
        EntityManager em = null;
        try {
            if (dataEnv.isClosed()) throw new DataEnvironmentClosedException(dataEnv.getDefinition().getEnvId());
            em = dataEnv.getEntityManagerFactory().createEntityManager(SYNCHRONIZED);
            return op.apply(em);
        } finally {
            if (em != null) em.close();
        }
    }

    private static Object extractRow(Object row) {
        if (row instanceof Object[]) {
            return Stream.of((Object[])row)
                    .map(NativeQueryAdapterImpl::extractRow)
                    .collect(toList());
        }

        return extractValue(row);
    }

    private static Object extractValue(Object cell) {
        if (cell instanceof Time)
            return ((Time)cell).toLocalTime();
        if (cell instanceof Date)
            return ((Date)cell).toLocalDate();
        if (cell instanceof Timestamp)
            return ((Timestamp)cell).toLocalDateTime();
        if (cell instanceof Clob)
            return extractLob((Clob)cell);
        if (cell instanceof Blob)
            return extractLob((Blob)cell);

        return cell;
    }

    private static String extractLob(Clob clob) {
        try {
            return clob.getSubString(1, (int)clob.length());
        } catch (SQLException e) {
            throw new DataRetrievalFailureException("cannot fetch LOB", e);
        }
    }

    private static byte[] extractLob(Blob blob) {
        try {
            return blob.getBytes(1, (int)blob.length());
        } catch (SQLException e) {
            throw new DataRetrievalFailureException("cannot fetch LOB", e);
        }
    }
}
