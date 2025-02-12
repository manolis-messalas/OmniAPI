package com.messalas.spring_boot_demo_A.service;

import com.messalas.spring_boot_demo_A.model.mappers.AuthorMapper;
import com.messalas.spring_boot_demo_A.model.dto.AuthorDTO;
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

    public AuthorEntity createAuthor(AuthorDTO authorDTO){
        AuthorEntity authorEntityToSave = AuthorMapper.INSTANCE.authorDTOToAuthorEntity(authorDTO);
        log.info("Saving..."+ authorEntityToSave.toString());
        return authorRepository.save(authorEntityToSave);
    }

    public List<AuthorDTO> getAllAuthors(){
        List<AuthorEntity> authorEntities = authorRepository.findAll();
        return authorEntities.stream().map(AuthorMapper.INSTANCE::authorEntityToAuthorDTO).toList();
    }

}
