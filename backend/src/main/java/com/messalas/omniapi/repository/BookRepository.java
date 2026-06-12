package com.messalas.omniapi.repository;

import com.messalas.omniapi.model.entities.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<BookEntity, Long> {

    boolean existsByAuthorEntityId(Long authorId);

    Optional<BookEntity> findByName(String name);

}
