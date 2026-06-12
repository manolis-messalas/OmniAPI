package com.messalas.omniapi.repository;

import com.messalas.omniapi.model.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsById(Long id);

    Optional<UserEntity> findByUsername(String username);

}
