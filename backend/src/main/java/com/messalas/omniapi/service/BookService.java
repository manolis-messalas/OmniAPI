package com.messalas.omniapi.service;

import com.messalas.omniapi.exceptions.OptimisticLockConflictException;
import com.messalas.omniapi.model.mappers.BookMapper;
import com.messalas.omniapi.model.dto.BookAuthorDTO;
import com.messalas.omniapi.model.dto.BookDTO;
import com.messalas.omniapi.model.entities.AuthorEntity;
import com.messalas.omniapi.model.entities.BookEntity;
import com.messalas.omniapi.repository.AuthorRepository;
import com.messalas.omniapi.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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

    @Transactional
    public Long saveBook(BookDTO bookDTO){
        AuthorEntity author = authorRepository.findByName(bookDTO.getAuthorDTO().getAuthorName())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Author not found with name: " + bookDTO.getAuthorDTO().getAuthorName()));
        BookEntity bookEntityToSave = new BookEntity();
        bookEntityToSave.setName(bookDTO.getBookName());
        bookEntityToSave.setPublicationYear(bookDTO.getPublicationYear());
        bookEntityToSave.setAuthorEntity(author);

        log.info("Saving..."+ bookEntityToSave.toString());
        return bookRepository.save(bookEntityToSave).getId();
    }

    public List<BookDTO> getAllBooks(){
        List<BookEntity> bookEntities = bookRepository.findAll();
        return bookEntities.stream().map(BookMapper.INSTANCE::bookEntityToBookDTO).toList();
    }

    public BookDTO findBookByName(String bookName){
        BookEntity bookEntity = bookRepository.findByName(bookName).
                orElseThrow(() -> new IllegalArgumentException("Book not found with name: " + bookName));
        return BookMapper.INSTANCE.bookEntityToBookDTO(bookEntity);
    }

    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new EntityNotFoundException("Book with id " + id + " not found");
        }
        bookRepository.deleteById(id);
    }

    @Transactional
    public BookDTO updateBook(Long id, BookDTO bookDTO) {
        if (bookDTO.getVersion() == null) {
            throw new IllegalArgumentException("version is required for update");
        }
        BookEntity book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with ID: " + id));
        book.setVersion(bookDTO.getVersion());
        book.setName(bookDTO.getBookName());
        book.setPublicationYear(bookDTO.getPublicationYear());
        if (bookDTO.getAuthorDTO() != null && bookDTO.getAuthorDTO().getAuthorName() != null) {
            AuthorEntity author = authorRepository.findByName(bookDTO.getAuthorDTO().getAuthorName())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Author not found with name: " + bookDTO.getAuthorDTO().getAuthorName()));
            book.setAuthorEntity(author);
        }
        try {
            BookEntity saved = bookRepository.saveAndFlush(book);
            return BookMapper.INSTANCE.bookEntityToBookDTO(saved);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new OptimisticLockConflictException(
                    "Book with ID " + id + " was modified by another transaction. Re-fetch and retry.");
        }
    }

}
