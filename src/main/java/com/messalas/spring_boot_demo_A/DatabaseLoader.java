package com.messalas.spring_boot_demo_A;

import com.messalas.spring_boot_demo_A.model.Author;
import com.messalas.spring_boot_demo_A.model.Book;
import com.messalas.spring_boot_demo_A.repository.AuthorRepository;
import com.messalas.spring_boot_demo_A.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@Order(value = 2)
public class DatabaseLoader {

    private static final Logger log = LoggerFactory.getLogger(DatabaseLoader.class);

    @Bean
    CommandLineRunner initDatabase(AuthorRepository authorRepository, BookRepository bookRepository) {
        return args -> {
            log.info("Populating data from Database Loader...");
            Author walterKaufmann =  authorRepository.save(new Author("Walter Kaufmann", "1 July 1921", "Germany"));
            log.info("Saving..." + walterKaufmann.toString());
            Book onTheGenealogyOfMorals = bookRepository.save(new Book("On the Genealogy of Morals and Ecce Homo", "1989", walterKaufmann));
            log.info("Saving..." + onTheGenealogyOfMorals.toString());
        };
    }

}
