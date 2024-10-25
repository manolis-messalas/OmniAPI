package com.messalas.spring_boot_demo_A.repository;

import com.messalas.spring_boot_demo_A.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository  extends JpaRepository<Author, Long> {
}
