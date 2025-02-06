package com.messalas.spring_boot_demo_A.mappers;

import com.messalas.spring_boot_demo_A.model.dto.AuthorDTO;
import com.messalas.spring_boot_demo_A.model.entities.AuthorEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthorMapper {

    AuthorMapper INSTANCE = Mappers.getMapper(AuthorMapper.class);

    @Mapping(source = "id", target = "authorId")
    @Mapping(source = "name", target = "authorName")
    @Mapping(source = "dateOfBirth", target = "dateOfBirth")
    @Mapping(source = "countryOfOrigin", target = "countryOfOrigin")
    AuthorDTO authorEntityToAuthorDTO(AuthorEntity author);

    @Mapping(source = "authorId", target = "id")
    @Mapping(source = "authorName", target = "name")
    @Mapping(source = "dateOfBirth", target = "dateOfBirth")
    @Mapping(source = "countryOfOrigin", target = "countryOfOrigin")
    AuthorEntity authorDTOToAuthorEntity(AuthorDTO authorDTO);

}
