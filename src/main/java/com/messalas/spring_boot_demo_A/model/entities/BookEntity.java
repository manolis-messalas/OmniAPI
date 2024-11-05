package com.messalas.spring_boot_demo_A.model.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "book")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String publicationYear;

    @ManyToOne
    @JoinColumn(name = "authorId")
    private AuthorEntity authorEntity;

    public BookEntity(String name, String publicationYear, AuthorEntity authorEntity) {
        this.name = name;
        this.publicationYear = publicationYear;
        this.authorEntity = authorEntity;
    }
}
