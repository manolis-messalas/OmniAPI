package com.messalas.spring_boot_demo_A.db;

import com.messalas.spring_boot_demo_A.dto.BookAuthorDTO;
import com.messalas.spring_boot_demo_A.service.BookInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(value = 1)
public class AnotherDatabaseLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AnotherDatabaseLoader.class);

    @Autowired
    private BookInfoService bookInfoService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Populating data from Another Database Loader...");
        bookInfoService.saveBookAuthor(new BookAuthorDTO("The Art of Thinking Clearly", "15 June 1966", "Switzerland", "Rolf Debolli", "2014"));
    }

}