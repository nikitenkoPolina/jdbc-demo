package dao;

import entity.Ticket;

import java.util.List;
import java.util.Optional;

public interface Dao<K, E> {

    boolean delete(K id);

    E save(Ticket ticket);

    List<E> findAll();

    Optional<E> findById(K id);

    void update(E ticket);

}
