package com.messalas.spring_boot_demo_A.db;

import com.messalas.spring_boot_demo_A.model.dto.BookAuthorDTO;
import com.messalas.spring_boot_demo_A.service.BookInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration(proxyBeanMethods=false)
@Order(value = 2)
public class DatabaseLoader {

    private static final Logger log = LoggerFactory.getLogger(DatabaseLoader.class);

    /* We are using a functional interface because the CommandLineRunner
    has only one method that we have to override ( run() )
    so we can do that we the lambda expression*/
    @Bean
    CommandLineRunner initDatabase(BookInfoService bookInfoService) {
        return args -> {
            log.info("Populating data from Database Loader...");
            bookInfoService.saveBookAuthor(new BookAuthorDTO("On the Genealogy of Morals and Ecce Homo", "1 July 1921", "Germany", "Walter Kaufmann", "1989"));
        };
    }

}
