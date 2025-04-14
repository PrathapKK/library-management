
//src/main/java/com/library/management/dto/BookDto.java
package com.codehawk.library.librarymanagement.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDto {
 private Long id;
 
 @NotEmpty(message = "Title cannot be empty")
 private String title;
 
 @NotEmpty(message = "Author cannot be empty")
 private String author;
 
 @NotEmpty(message = "ISBN cannot be empty")
 private String isbn;
 
 private String publisher;
 
 private LocalDate publicationDate;
 
 private String category;
 
 @NotNull(message = "Total copies must be specified")
 @Positive(message = "Total copies must be positive")
 private int totalCopies;
}