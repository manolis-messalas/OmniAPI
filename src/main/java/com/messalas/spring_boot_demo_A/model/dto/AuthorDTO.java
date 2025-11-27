package com.messalas.spring_boot_demo_A.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthorDTO {

    public AuthorDTO(String authorName, String dateOfBirth, String countryOfOrigin) {
        this.authorName = authorName;
        this.dateOfBirth = dateOfBirth;
        this.countryOfOrigin = countryOfOrigin;
    }

    private Long authorId;
    private String authorName;
    private String dateOfBirth;
    private String countryOfOrigin;

}