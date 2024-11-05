package com.messalas.spring_boot_demo_A.builders;

import com.messalas.spring_boot_demo_A.model.entities.AuthorEntity;

public class AuthorBuilder {

    private String name;
    private String dateOfBirth;
    private String countryOfOrigin;

    public AuthorBuilder name(String name){
        this.name = name;
        return this;
    }

    public AuthorBuilder dateOfBirth(String dateOfBirth){
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public AuthorBuilder countryOfOrigin(String countryOfOrigin){
        this.countryOfOrigin = countryOfOrigin;
        return this;
    }

    public AuthorEntity build(){
        return new AuthorEntity(name, dateOfBirth, countryOfOrigin);
    }

}
