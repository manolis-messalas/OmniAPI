package com.messalas.spring_boot_demo_A.unit.mappers;

import com.messalas.spring_boot_demo_A.model.mappers.AuthorMapper;
import com.messalas.spring_boot_demo_A.model.dto.AuthorDTO;
import com.messalas.spring_boot_demo_A.model.entities.AuthorEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorMapperTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthorMapperTest.class);

    private AuthorEntity authorEntity;
    private AuthorDTO authorDTO;

    @BeforeEach
    public void setUp() {
        //Mock Data for testAuthorEntityToAuthorDTO
        authorEntity = AuthorEntity.builder().
                id(3L).
                name("author test 3").
                countryOfOrigin("testland3").
                dateOfBirth("02.02.1996").
                build();

        //Mock Data for testAuthorDTOToAuthorEntity
        authorDTO = AuthorDTO.builder().
                authorId(3L).
                authorName("author test 4").
                countryOfOrigin("testland4").
                dateOfBirth("09.08.2000")
                .build();
    }

    @Test
    public void testAuthorEntityToAuthorDTO(){
        logger.info("Testing testAuthorEntityToAuthorDTO...");
        try {
            AuthorDTO authorDTOToTest = AuthorMapper.INSTANCE.authorEntityToAuthorDTO(authorEntity);
            logger.info("AuthorMapper instance created");

            Assertions.assertNotNull(authorDTOToTest);
            Assertions.assertEquals(authorEntity.getId(), authorDTOToTest.getAuthorId());
            Assertions.assertEquals(authorEntity.getName(), authorDTOToTest.getAuthorName());
            Assertions.assertEquals(authorEntity.getCountryOfOrigin(), authorDTOToTest.getCountryOfOrigin());
            Assertions.assertEquals(authorEntity.getDateOfBirth(), authorDTOToTest.getDateOfBirth());
            logger.info("testAuthorEntityToAuthorDTO passed successfully");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void testAuthorDTOToAuthorEntity(){
        logger.info("Testing testAuthorDTOToAuthorEntity...");
        try {
            AuthorEntity authorEntityToTest = AuthorMapper.INSTANCE.authorDTOToAuthorEntity(authorDTO);
            logger.info("AuthorMapper instance created");

            Assertions.assertNotNull(authorEntityToTest);
            Assertions.assertEquals(authorEntityToTest.getId(), authorDTO.getAuthorId());
            Assertions.assertEquals(authorEntityToTest.getName(), authorDTO.getAuthorName());
            Assertions.assertEquals(authorEntityToTest.getCountryOfOrigin(), authorDTO.getCountryOfOrigin());
            Assertions.assertEquals(authorEntityToTest.getDateOfBirth(), authorDTO.getDateOfBirth());
            logger.info("testAuthorEntityToAuthorDTO passed successfully");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

}