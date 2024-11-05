package com.messalas.spring_boot_demo_A.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookDTO {

    private Long id;
    private String bookName;
    private String publicationYear;
    private AuthorDTO authorDTO;

}
