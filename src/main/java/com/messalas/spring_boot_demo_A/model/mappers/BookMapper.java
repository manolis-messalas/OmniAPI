package com.messalas.spring_boot_demo_A.model.mappers;

import com.messalas.spring_boot_demo_A.model.dto.BookDTO;
import com.messalas.spring_boot_demo_A.model.entities.BookEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {AuthorMapper.class})
public interface BookMapper {

    BookMapper INSTANCE = Mappers.getMapper(BookMapper.class);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "bookName")
    @Mapping(source = "publicationYear", target = "publicationYear")
    @Mapping(source = "authorEntity", target = "authorDTO")
    BookDTO bookEntityToBookDTO(BookEntity bookEntity);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "bookName", target = "name")
    @Mapping(source = "publicationYear", target = "publicationYear")
    @Mapping(source = "authorDTO", target = "authorEntity")
    BookEntity bookDTOtoBookEntity(BookDTO bookDTO);

}
