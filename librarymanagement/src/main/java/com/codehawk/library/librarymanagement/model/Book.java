
/*
 * Library Management System - Spring Boot Project
 * 
 * This project includes:
 * - Entity models for Books, Users, and Transactions
 * - Repositories for database operations
 * - Service layer for business logic
 * - Controllers for API endpoints
 * - Basic exception handling
 * - Application properties configuration
 */

// ---------- Entity Models ----------

// src/main/java/com/library/management/model/Book.java
package com.codehawk.library.librarymanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String author;
    
    @Column(nullable = false, unique = true)
    private String isbn;
    
    private String publisher;
    
    private LocalDate publicationDate;
    
    private String category;
    
    @Column(nullable = false)
    private boolean available = true;
    
    private int totalCopies;
    
    private int availableCopies;
}