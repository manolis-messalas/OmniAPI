package com.messalas.spring_boot_demo_A.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookAuthorDTO {

    private String bookName;
    private String dateOfBirth;
    private String countryOfOrigin;
    private String authorName;
    private String publicationYear;

}
