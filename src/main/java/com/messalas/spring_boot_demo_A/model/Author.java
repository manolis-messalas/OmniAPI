package com.messalas.spring_boot_demo_A.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "author")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String dateOfBirth;

    @Column(nullable = false)
    private String countryOfOrigin;

    public Author(String name, String dateOfBirth, String countryOfOrigin) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.countryOfOrigin = countryOfOrigin;
    }
}
