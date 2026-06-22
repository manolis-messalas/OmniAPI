package com.messalas.omniapi.db;

import com.messalas.omniapi.model.dto.BookAuthorDTO;
import com.messalas.omniapi.repository.BookRepository;
import com.messalas.omniapi.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

@Configuration(proxyBeanMethods = false)
@Profile("postgres")
@Order(value = 3)
public class PostgresDatabaseLoader {

    private static final Logger log = LoggerFactory.getLogger(PostgresDatabaseLoader.class);

    @Bean
    CommandLineRunner initPostgresDatabase(BookService bookService, BookRepository bookRepository) {
        return args -> {
            if (bookRepository.count() > 0) {
                log.info("PostgreSQL seed data already present, skipping.");
                return;
            }

            log.info("Loading PostgreSQL seed data...");

            bookService.saveBookAuthor(new BookAuthorDTO(
                    "The Fear of Freedom",
                    "23 March 1900",
                    "German",
                    "Erich Fromm",
                    "1941"
            ));

            bookService.saveBookAuthor(new BookAuthorDTO(
                    "The Body",
                    "8 December 1951",
                    "USA",
                    "Bill Bryson",
                    "2021"
            ));

            bookService.saveBookAuthor(new BookAuthorDTO(
                    "Crime and Punishment",
                    "11 November 1821",
                    "Russia",
                    "Fyodor Dostoevsky",
                    "1865"
            ));

            bookService.saveBookAuthor(new BookAuthorDTO(
                    "The Poems",
                    "29 April 1863",
                    "Greece",
                    "Konstantinos Kavafis",
                    "1963"
            ));

            bookService.saveBookAuthor(new BookAuthorDTO(
                    "Prisoners of Geography",
                    "1 May 1959",
                    "UK",
                    "Tim Marshall",
                    "2015"
            ));

            bookService.saveBookAuthor(new BookAuthorDTO(
                    "Sapiens: A Brief History of Humankind",
                    "24 February 1976",
                    "Israel",
                    "Yuval Noah Harari",
                    "2015"
            ));

            log.info("PostgreSQL seed data loaded: 6 books with authors.");
        };
    }
}


