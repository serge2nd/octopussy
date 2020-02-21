package ru.serge2nd.octopussy.data;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.SynchronizationType;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class NativeQueryAdapterImpl implements NativeQueryAdapter {
    private final EntityManagerFactory entityManagerFactory;

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
            em = entityManagerFactory.createEntityManager(SynchronizationType.SYNCHRONIZED);
            return op.apply(em);
        } finally {
            if (em != null) em.close();
        }
    }

    private static Object extractRow(Object row) {
        if (row instanceof Object[]) {
            return Stream.of((Object[])row)
                    .map(NativeQueryAdapterImpl::extractValue)
                    .collect(toList());
        }

        return extractValue(row);
    }

    private static Object extractValue(Object cell) {
        if (!(cell instanceof Clob))
            return cell;

        Clob clob = (Clob) cell;

        try {
            return clob.getSubString(1, (int)clob.length());
        } catch (SQLException e) {
            throw new DataRetrievalFailureException("cannot obtain LOB", e);
        }
    }
}
