package com.messalas.spring_boot_demo_A.unit.mappers;

import com.messalas.spring_boot_demo_A.model.mappers.BookMapper;
import com.messalas.spring_boot_demo_A.model.dto.AuthorDTO;
import com.messalas.spring_boot_demo_A.model.dto.BookDTO;
import com.messalas.spring_boot_demo_A.model.entities.AuthorEntity;
import com.messalas.spring_boot_demo_A.model.entities.BookEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookMapperTest {

    private static final Logger logger = LoggerFactory.getLogger(BookMapperTest.class);

    private BookEntity bookEntity;
    private BookDTO bookDTO;

    @BeforeEach
    public void setUp(){
        // Mock Data for testBookEntityToBookDTO
        AuthorEntity authorEntity = AuthorEntity.builder().
                id(1L).
                name("author test 1").
                dateOfBirth("12.03.1993").
                countryOfOrigin("testland1").
                build();

        bookEntity = BookEntity.builder().
                id(11L).
                name("book test 1").
                publicationYear("6969").
                authorEntity(authorEntity).
                build();

        // Mock Data for testBookDTOToBookEntity
        AuthorDTO authorDTO = AuthorDTO.builder()
                .authorId(2L)
                .authorName("author test 2")
                .dateOfBirth("12.03.1994")
                .countryOfOrigin("testland2")
                .build();

        bookDTO = BookDTO.builder()
                .id(22L)
                .bookName("book test 2")
                .publicationYear("7272")
                .authorDTO(authorDTO)
            .build();
    }

    @Test
    public void testBookEntityToBookDTO() {
        logger.info("Testing authorEntityToAuthorDTO...");
        try {
            BookDTO bookDTOToTest = BookMapper.INSTANCE.bookEntityToBookDTO(bookEntity);
            logger.info("BookMapper instance created");

            Assertions.assertNotNull(bookDTO);
            Assertions.assertEquals(bookEntity.getId(), bookDTOToTest.getId());
            Assertions.assertEquals(bookEntity.getName(), bookDTOToTest.getBookName());
            Assertions.assertEquals(bookEntity.getPublicationYear(), bookDTOToTest.getPublicationYear());
            // Check nested AuthorDTO mapping
            Assertions.assertNotNull(bookDTOToTest.getAuthorDTO());
            Assertions.assertEquals(bookEntity.getAuthorEntity().getId(), bookDTOToTest.getAuthorDTO().getAuthorId());
            Assertions.assertEquals(bookEntity.getAuthorEntity().getName(), bookDTOToTest.getAuthorDTO().getAuthorName());
            logger.info("testBookEntityToBookDTO passed successfully");
        } catch (Exception e){
            logger.info(e.getMessage());
        }
    }

    @Test
    public void testBookDTOToBookEntity() {
        logger.info("Testing BookDTOToBookEntity...");
        try{
            BookEntity bookEntityToTest  = BookMapper.INSTANCE.bookDTOtoBookEntity(bookDTO);
            logger.info("BookMapper instance created");

            Assertions.assertNotNull(bookEntityToTest);
            Assertions.assertEquals(bookEntityToTest.getId(), bookDTO.getId());
            Assertions.assertEquals(bookEntityToTest.getName(), bookDTO.getBookName());
            Assertions.assertEquals(bookEntityToTest.getPublicationYear(), bookDTO.getPublicationYear());
            // Check nested AuthorDTO mapping
            Assertions.assertNotNull(bookEntityToTest.getAuthorEntity());
            Assertions.assertEquals(bookEntityToTest.getAuthorEntity().getId(), bookDTO.getAuthorDTO().getAuthorId());
            Assertions.assertEquals(bookEntityToTest.getAuthorEntity().getName(), bookDTO.getAuthorDTO().getAuthorName());
            logger.info("testBookDTOToBookEntity passed successfully");
        } catch (Exception e){
            logger.info(e.getMessage());
        }
    }

}
