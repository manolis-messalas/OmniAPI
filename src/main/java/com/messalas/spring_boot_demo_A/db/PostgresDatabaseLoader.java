package com.messalas.spring_boot_demo_A.db;

import com.messalas.spring_boot_demo_A.model.dto.BookAuthorDTO;
import com.messalas.spring_boot_demo_A.model.dto.UserDetails;
import com.messalas.spring_boot_demo_A.service.BookService;
import com.messalas.spring_boot_demo_A.service.UserService;
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
    CommandLineRunner initPostgresDatabase(BookService bookService, UserService userService) {
        return args -> {
            log.info("Loading PostgreSQL seed data after Hibernate table creation...");
            
            try {

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

                userService.saveUser(new UserDetails(
                                 "ManoloAdmin",
                        "@@password!!90",
                        "ADMIN"
                ));

                log.info("PostgreSQL seed data loading complete! Inserted 6 books with authors.");

            } catch (Exception e) {
                log.warn("PostgreSQL seed data already exists or error occurred: {}", e.getMessage());
                log.debug("This is expected if data was already loaded in previous runs", e);
            }
        };
    }
}


