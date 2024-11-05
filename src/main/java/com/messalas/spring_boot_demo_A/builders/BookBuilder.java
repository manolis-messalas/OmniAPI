package com.messalas.spring_boot_demo_A.builders;

import com.messalas.spring_boot_demo_A.model.entities.AuthorEntity;
import com.messalas.spring_boot_demo_A.model.entities.BookEntity;

public class BookBuilder {

    private String name;
    private String publicationYear;
    private AuthorEntity authorEntity;

    public BookBuilder name(String name) {
        this.name = name;
        return this;
    }

    public BookBuilder publicationYear(String publicationYear){
        this.publicationYear = publicationYear;
        return this;
    }

    public BookBuilder author(AuthorEntity authorEntity){
        this.authorEntity = authorEntity;
        return this;
    }

    public BookEntity build(){
        return new BookEntity(name, publicationYear, authorEntity);
    }


}
