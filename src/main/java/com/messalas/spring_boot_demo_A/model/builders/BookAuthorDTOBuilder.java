package com.messalas.spring_boot_demo_A.model.builders;

import com.messalas.spring_boot_demo_A.model.dto.BookAuthorDTO;

public class BookAuthorDTOBuilder {

    private String bookName;
    private String dateOfBirth;
    private String countryOfOrigin;
    private String authorName;
    private String publicationYear;

    public BookAuthorDTOBuilder bookName(String bookName){
        this.bookName = bookName;
        return this;
    }

    public BookAuthorDTOBuilder dateOfBirth(String dateOfBirth){
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public BookAuthorDTOBuilder countryOfOrigin(String countryOfOrigin){
        this.countryOfOrigin = countryOfOrigin;
        return this;
    }

    public BookAuthorDTOBuilder authorName(String authorName){
        this.authorName = authorName;
        return this;
    }

    public BookAuthorDTOBuilder publicationYear(String publicationYear){
        this.publicationYear = publicationYear;
        return this;
    }

    public BookAuthorDTO build(){
        return new BookAuthorDTO(bookName,dateOfBirth, countryOfOrigin, authorName, publicationYear);
    }

}
