package com.messalas.spring_boot_demo_A.service;

import com.messalas.spring_boot_demo_A.dto.BookAuthorDTO;
import com.messalas.spring_boot_demo_A.model.Author;
import com.messalas.spring_boot_demo_A.model.Book;
import com.messalas.spring_boot_demo_A.repository.AuthorRepository;
import com.messalas.spring_boot_demo_A.repository.BookRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookInfoService{

    private static final Logger log = LoggerFactory.getLogger(BookInfoService.class);

    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private BookRepository bookRepository;

    @Transactional
    public void saveBookAuthor(BookAuthorDTO bookAuthorDTO){
        log.info("Saving..."+ bookAuthorDTO.toString());
        Author authorToSave = new Author(bookAuthorDTO.getAuthorName(), bookAuthorDTO.getDateOfBirth(), bookAuthorDTO.getCountryOfOrigin());
        authorToSave = authorRepository.save(authorToSave);
        bookRepository.save(new Book(bookAuthorDTO.getBookName(), bookAuthorDTO.getPublicationYear(), authorToSave));
    }

    public List<Book> getAllBooks(){
        return bookRepository.findAll();
    }

    public List<Author> getAllAuthors(){
        return authorRepository.findAll();
    }


}
