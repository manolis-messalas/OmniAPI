package com.messalas.spring_boot_demo_A.repository;

import com.messalas.spring_boot_demo_A.model.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsById(Long id);

    Optional<UserEntity> findByUsername(String username);

}
