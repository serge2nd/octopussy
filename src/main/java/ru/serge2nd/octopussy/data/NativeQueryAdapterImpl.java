package ru.serge2nd.octopussy.data;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class NativeQueryAdapterImpl implements NativeQueryAdapter {
    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<?> execute(String query) {
        List<?> resultList = entityManager.createNativeQuery(query).getResultList();
        return resultList.stream()
                .map(NativeQueryAdapterImpl::extractRow)
                .collect(toList());
    }

    @Transactional
    public int executeUpdate(List<String> queries) {
        return queries.stream()
                .mapToInt(query -> entityManager.createNativeQuery(query).executeUpdate())
                .sum();
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
