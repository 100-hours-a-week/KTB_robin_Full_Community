package ktb3.fullstack.week4.repository;

import java.util.Optional;

public interface CrudRepository<T, ID> {
    void save(T entity);
    Optional<T> findById(ID id);
    boolean deleteById(ID id);
}
