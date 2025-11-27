package com.messalas.spring_boot_demo_A.repository;

import com.messalas.spring_boot_demo_A.model.entities.AuthorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorRepository  extends JpaRepository<AuthorEntity, Long> {

    Optional<AuthorEntity> findByName(String name);

}
