
//---------- Utilities ----------

//src/main/java/com/library/management/util/DataInitializer.java
package com.codehawk.library.librarymanagement.util;

import com.codehawk.library.librarymanagement.model.Book;
import com.codehawk.library.librarymanagement.model.User;
import com.codehawk.library.librarymanagement.repository.BookRepository;
import com.codehawk.library.librarymanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import lombok.*;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

 private final UserRepository userRepository;
 private final BookRepository bookRepository;
 private final PasswordEncoder passwordEncoder;

 @Autowired
 public DataInitializer(UserRepository userRepository, BookRepository bookRepository, 
                       PasswordEncoder passwordEncoder) {
     this.userRepository = userRepository;
     this.bookRepository = bookRepository;
     this.passwordEncoder = passwordEncoder;
 }

 @Override
 public void run(String... args) {
     // Create admin user if it doesn't exist
     if (!userRepository.existsByEmail("admin@library.com")) {
         User admin = new User();
         admin.setName("Admin");
         admin.setEmail("admin@library.com");
         admin.setPassword(passwordEncoder.encode("admin123"));
         admin.setRole(User.Role.ADMIN);
         userRepository.save(admin);
     }
     
     // Create librarian user if it doesn't exist
     if (!userRepository.existsByEmail("librarian@library.com")) {
         User librarian = new User();
         librarian.setName("Librarian");
         librarian.setEmail("librarian@library.com");
         librarian.setPassword(passwordEncoder.encode("lib123"));
         librarian.setRole(User.Role.LIBRARIAN);
         userRepository.save(librarian);
     }
     
     // Create sample user if it doesn't exist
     if (!userRepository.existsByEmail("user@example.com")) {
         User user = new User();
         user.setName("Sample User");
         user.setEmail("user@example.com");
         user.setPassword(passwordEncoder.encode("user123"));
         user.setRole(User.Role.USER);
         userRepository.save(user);
     }
     
     // Add some sample books if the repository is empty
     if (bookRepository.count() == 0) {
         // Sample book 1
         Book book1 = new Book();
         book1.setTitle("To Kill a Mockingbird");
         book1.setAuthor("Harper Lee");
         book1.setIsbn("9780061120084");
         book1.setPublisher("HarperCollins");
         book1.setPublicationDate(LocalDate.of(1960, 7, 11));
         book1.setCategory("Fiction");
         book1.setTotalCopies(5);
         book1.setAvailableCopies(5);
         book1.setAvailable(true);
         bookRepository.save(book1);
         
         // Sample book 2
         Book book2 = new Book();
         book2.setTitle("1984");
         book2.setAuthor("George Orwell");
         book2.setIsbn("9780451524935");
         book2.setPublisher("Signet Classics");
         book2.setPublicationDate(LocalDate.of(1949, 6, 8));
         book2.setCategory("Fiction");
         book2.setTotalCopies(3);
         book2.setAvailableCopies(3);
         book2.setAvailable(true);
         bookRepository.save(book2);
         
         // Sample book 3
         Book book3 = new Book();
         book3.setTitle("The Great Gatsby");
         book3.setAuthor("F. Scott Fitzgerald");
         book3.setIsbn("9780743273565");
         book3.setPublisher("Scribner");
         book3.setPublicationDate(LocalDate.of(1925, 4, 10));
         book3.setCategory("Fiction");
         book3.setTotalCopies(4);
         book3.setAvailableCopies(4);
         book3.setAvailable(true);
         bookRepository.save(book3);
     }
 }
}
