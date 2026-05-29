package edu.unac.repository;

import edu.unac.model.FavoriteBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteBookRepository extends JpaRepository<FavoriteBook, Long> {
    boolean existsByBookKey(String bookKey);
}