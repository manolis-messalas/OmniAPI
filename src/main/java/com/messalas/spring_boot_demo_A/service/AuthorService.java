package com.messalas.spring_boot_demo_A.service;

import com.messalas.spring_boot_demo_A.model.mappers.AuthorMapper;
import com.messalas.spring_boot_demo_A.model.dto.AuthorDTO;
import com.messalas.spring_boot_demo_A.model.entities.AuthorEntity;
import com.messalas.spring_boot_demo_A.repository.AuthorRepository;
import com.messalas.spring_boot_demo_A.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorService {

    private static final Logger log = LoggerFactory.getLogger(AuthorService.class);

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    public Long createAuthor(AuthorDTO authorDTO){
        AuthorEntity authorEntityToSave = AuthorMapper.INSTANCE.authorDTOToAuthorEntity(authorDTO);
        log.info("Saving..."+ authorEntityToSave.toString());
        return authorRepository.save(authorEntityToSave).getId();
    }

    public List<AuthorDTO> getAllAuthors(){
        List<AuthorEntity> authorEntities = authorRepository.findAll();
        return authorEntities.stream().map(AuthorMapper.INSTANCE::authorEntityToAuthorDTO).toList();
    }

    @Transactional
    public void deleteAuthor(Long id) {
        AuthorEntity author = authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Author not found"));

        if (bookRepository.existsByAuthorEntityId(id)) {
            throw new IllegalStateException(
                    "Cannot delete author because books reference this author"
            );
        }

        authorRepository.delete(author);
    }

    public AuthorDTO getAuthorById(Long authorId) {
        AuthorEntity authorEntity = authorRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found with ID: " + authorId));
        return AuthorMapper.INSTANCE.authorEntityToAuthorDTO(authorEntity);
    }

}
