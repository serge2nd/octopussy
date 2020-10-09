package ru.serge2nd.octopussy.support;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.transform.ResultTransformer;
import org.springframework.dao.DataRetrievalFailureException;
import ru.serge2nd.collection.Unmodifiable;

import java.sql.*;
import java.util.List;

@Slf4j
public enum ToListResultTransformer implements ResultTransformer {
    INSTANCE;

    @Override
    public Object transformTuple(Object[] tuple, String[] aliases) {
        if (tuple.length == 1)
            return extractValue(tuple[0]);
        return extractRow(tuple);
    }
    @Override
    public List<?> transformList(List list) {
        return Unmodifiable.ofList(list);
    }

    private static List<?> extractRow(Object[] row) {
        for (int i = 0; i < row.length; i++)
            row[i] = row[i] instanceof Object[]
                ? extractRow((Object[])row[i])
                : extractValue(row[i]);
        return Unmodifiable.of(row);
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
            throw new DataRetrievalFailureException("cannot fetch CLOB", e);
        } finally { try {
            clob.free();
        } catch (SQLException e) {
            log.warn("cannot free CLOB", e);
        }}
    }

    private static byte[] extractLob(Blob blob) {
        try {
            return blob.getBytes(1, (int)blob.length());
        } catch (SQLException e) {
            throw new DataRetrievalFailureException("cannot fetch BLOB", e);
        } finally { try {
            blob.free();
        } catch (SQLException e) {
            log.warn("cannot free BLOB", e);
        }}
    }
}
