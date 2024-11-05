package com.messalas.spring_boot_demo_A.service;

import com.messalas.spring_boot_demo_A.model.entities.AuthorEntity;
import com.messalas.spring_boot_demo_A.repository.AuthorRepository;
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

    public AuthorEntity createAuthor(AuthorEntity authorEntity){
        log.info("Saving..."+ authorEntity.toString());
        return authorRepository.save(authorEntity);
    }

    public List<AuthorEntity> getAllAuthors(){
        return authorRepository.findAll();
    }

}
