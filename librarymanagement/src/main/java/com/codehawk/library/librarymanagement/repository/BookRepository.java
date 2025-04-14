

//src/main/java/com/library/management/repository/BookRepository.java
package com.codehawk.library.librarymanagement.repository;

import com.codehawk.library.librarymanagement.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
 List<Book> findByTitleContainingIgnoreCase(String title);
 List<Book> findByAuthorContainingIgnoreCase(String author);
 List<Book> findByCategoryContainingIgnoreCase(String category);
 Optional<Book> findByIsbn(String isbn);
 List<Book> findByAvailable(boolean available);
}
