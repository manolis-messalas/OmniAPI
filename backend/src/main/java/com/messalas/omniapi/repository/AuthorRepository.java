package com.messalas.omniapi.repository;

import com.messalas.omniapi.model.entities.AuthorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorRepository  extends JpaRepository<AuthorEntity, Long> {

    Optional<AuthorEntity> findByName(String name);

}
