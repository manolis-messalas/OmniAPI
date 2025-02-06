package com.messalas.spring_boot_demo_A.service;

import com.messalas.spring_boot_demo_A.mappers.BookMapper;
import com.messalas.spring_boot_demo_A.model.dto.BookAuthorDTO;
import com.messalas.spring_boot_demo_A.model.dto.BookDTO;
import com.messalas.spring_boot_demo_A.model.entities.AuthorEntity;
import com.messalas.spring_boot_demo_A.model.entities.BookEntity;
import com.messalas.spring_boot_demo_A.repository.AuthorRepository;
import com.messalas.spring_boot_demo_A.repository.BookRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private BookRepository bookRepository;

    @Transactional
    public Long saveBookAuthor(BookAuthorDTO bookAuthorDTO){
        log.info("Saving..."+ bookAuthorDTO.toString());
        AuthorEntity authorEntityToSave = new AuthorEntity(bookAuthorDTO.getAuthorName(), bookAuthorDTO.getDateOfBirth(), bookAuthorDTO.getCountryOfOrigin());
        authorEntityToSave = authorRepository.save(authorEntityToSave);
        BookEntity bookEntitySaved = bookRepository.save(new BookEntity(bookAuthorDTO.getBookName(), bookAuthorDTO.getPublicationYear(), authorEntityToSave));
        return bookEntitySaved.getId();
    }

    public List<BookDTO> getAllBooks(){
        List<BookEntity> bookEntities = bookRepository.findAll();
        return bookEntities.stream().map(BookMapper.INSTANCE::bookEntityToBookDTO).toList();
    }


}
