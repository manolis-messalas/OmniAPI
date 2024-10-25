package com.messalas.spring_boot_demo_A.repository;

import com.messalas.spring_boot_demo_A.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository  extends JpaRepository<Book, Long> {
}
