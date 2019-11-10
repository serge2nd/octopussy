package ru.serge2nd.octopussy.data;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@RequiredArgsConstructor
public class NativeQueryAdapterImpl implements NativeQueryAdapter {
    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<?> execute(String query) {
        return entityManager.createNativeQuery(query).getResultList();
    }

    @Transactional
    public int executeUpdate(List<String> queries) {
        return queries.stream()
                .mapToInt(query -> entityManager.createNativeQuery(query).executeUpdate())
                .sum();
    }
}
