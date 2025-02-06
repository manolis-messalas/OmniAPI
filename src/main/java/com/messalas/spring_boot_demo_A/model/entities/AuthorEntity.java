package com.messalas.spring_boot_demo_A.model.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "author")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String dateOfBirth;

    @Column(nullable = false)
    private String countryOfOrigin;

    public AuthorEntity(String name, String dateOfBirth, String countryOfOrigin) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.countryOfOrigin = countryOfOrigin;
    }
}
