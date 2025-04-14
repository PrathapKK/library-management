
package com.codehawk.library.librarymanagement.repository;

import com.codehawk.library.librarymanagement.model.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    @Test
    void shouldFindBookByIsbn() {
        // given
        Book book = new Book();
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setIsbn("1234567890");
        book.setPublisher("Test Publisher");
        book.setPublicationDate(LocalDate.now());
        book.setCategory("Test Category");
        book.setTotalCopies(5);
        book.setAvailableCopies(5);
        book.setAvailable(true);
        
        entityManager.persist(book);
        entityManager.flush();

        // when
        Optional<Book> found = bookRepository.findByIsbn("1234567890");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Book");
    }

    @Test
    void shouldNotFindBookByNonExistingIsbn() {
        // when
        Optional<Book> found = bookRepository.findByIsbn("nonexisting");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindBooksByTitleContainingIgnoreCase() {
        // given
        Book book1 = new Book();
        book1.setTitle("Java Programming");
        book1.setAuthor("Author 1");
        book1.setIsbn("1111111111");
        book1.setAvailable(true);
        book1.setTotalCopies(3);
        book1.setAvailableCopies(3);
        
        Book book2 = new Book();
        book2.setTitle("Advanced Java");
        book2.setAuthor("Author 2");
        book2.setIsbn("2222222222");
        book2.setAvailable(true);
        book2.setTotalCopies(2);
        book2.setAvailableCopies(2);
        
        Book book3 = new Book();
        book3.setTitle("Python Basics");
        book3.setAuthor("Author 3");
        book3.setIsbn("3333333333");
        book3.setAvailable(true);
        book3.setTotalCopies(1);
        book3.setAvailableCopies(1);
        
        entityManager.persist(book1);
        entityManager.persist(book2);
        entityManager.persist(book3);
        entityManager.flush();

        // when
        List<Book> javaBooks = bookRepository.findByTitleContainingIgnoreCase("java");

        // then
        assertThat(javaBooks).hasSize(2);
        assertThat(javaBooks).extracting(Book::getTitle)
                             .containsExactlyInAnyOrder("Java Programming", "Advanced Java");
    }

    @Test
    void shouldFindBooksByAuthorContainingIgnoreCase() {
        // given
        Book book1 = new Book();
        book1.setTitle("Book 1");
        book1.setAuthor("John Doe");
        book1.setIsbn("1111111111");
        book1.setAvailable(true);
        book1.setTotalCopies(3);
        book1.setAvailableCopies(3);
        
        Book book2 = new Book();
        book2.setTitle("Book 2");
        book2.setAuthor("John Smith");
        book2.setIsbn("2222222222");
        book2.setAvailable(true);
        book2.setTotalCopies(2);
        book2.setAvailableCopies(2);
        
        Book book3 = new Book();
        book3.setTitle("Book 3");
        book3.setAuthor("Jane Doe");
        book3.setIsbn("3333333333");
        book3.setAvailable(true);
        book3.setTotalCopies(1);
        book3.setAvailableCopies(1);
        
        entityManager.persist(book1);
        entityManager.persist(book2);
        entityManager.persist(book3);
        entityManager.flush();

        // when
        List<Book> johnBooks = bookRepository.findByAuthorContainingIgnoreCase("john");

        // then
        assertThat(johnBooks).hasSize(2);
        assertThat(johnBooks).extracting(Book::getAuthor)
                             .containsExactlyInAnyOrder("John Doe", "John Smith");
    }

    @Test
    void shouldFindBooksByCategoryContainingIgnoreCase() {
        // given
        Book book1 = new Book();
        book1.setTitle("Book 1");
        book1.setAuthor("Author 1");
        book1.setIsbn("1111111111");
        book1.setCategory("Science Fiction");
        book1.setAvailable(true);
        book1.setTotalCopies(3);
        book1.setAvailableCopies(3);
        
        Book book2 = new Book();
        book2.setTitle("Book 2");
        book2.setAuthor("Author 2");
        book2.setIsbn("2222222222");
        book2.setCategory("Science");
        book2.setAvailable(true);
        book2.setTotalCopies(2);
        book2.setAvailableCopies(2);
        
        Book book3 = new Book();
        book3.setTitle("Book 3");
        book3.setAuthor("Author 3");
        book3.setIsbn("3333333333");
        book3.setCategory("Fiction");
        book3.setAvailable(true);
        book3.setTotalCopies(1);
        book3.setAvailableCopies(1);
        
        entityManager.persist(book1);
        entityManager.persist(book2);
        entityManager.persist(book3);
        entityManager.flush();

        // when
        List<Book> scienceBooks = bookRepository.findByCategoryContainingIgnoreCase("science");

        // then
        assertThat(scienceBooks).hasSize(2);
        assertThat(scienceBooks).extracting(Book::getCategory)
                               .containsExactlyInAnyOrder("Science Fiction", "Science");
    }

    @Test
    void shouldFindByAvailable() {
        // given
        Book availableBook = new Book();
        availableBook.setTitle("Available Book");
        availableBook.setAuthor("Author 1");
        availableBook.setIsbn("1111111111");
        availableBook.setAvailable(true);
        availableBook.setTotalCopies(3);
        availableBook.setAvailableCopies(3);
        
        Book unavailableBook = new Book();
        unavailableBook.setTitle("Unavailable Book");
        unavailableBook.setAuthor("Author 2");
        unavailableBook.setIsbn("2222222222");
        unavailableBook.setAvailable(false);
        unavailableBook.setTotalCopies(2);
        unavailableBook.setAvailableCopies(0);
        
        entityManager.persist(availableBook);
        entityManager.persist(unavailableBook);
        entityManager.flush();

        // when
        List<Book> available = bookRepository.findByAvailable(true);
        List<Book> unavailable = bookRepository.findByAvailable(false);

        // then
        assertThat(available).hasSize(1);
        assertThat(available.get(0).getTitle()).isEqualTo("Available Book");
        
        assertThat(unavailable).hasSize(1);
        assertThat(unavailable.get(0).getTitle()).isEqualTo("Unavailable Book");
    }

    @Test
    void shouldSaveBook() {
        // given
        Book book = new Book();
        book.setTitle("New Book");
        book.setAuthor("New Author");
        book.setIsbn("9876543210");
        book.setPublisher("New Publisher");
        book.setPublicationDate(LocalDate.now());
        book.setCategory("New Category");
        book.setTotalCopies(10);
        book.setAvailableCopies(10);
        book.setAvailable(true);

        // when
        Book savedBook = bookRepository.save(book);

        // then
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getTitle()).isEqualTo("New Book");
        assertThat(savedBook.getAuthor()).isEqualTo("New Author");
        assertThat(savedBook.getIsbn()).isEqualTo("9876543210");
    }

    @Test
    void shouldUpdateBook() {
        // given
        Book book = new Book();
        book.setTitle("Original Title");
        book.setAuthor("Original Author");
        book.setIsbn("1122334455");
        book.setAvailable(true);
        book.setTotalCopies(5);
        book.setAvailableCopies(5);
        
        entityManager.persist(book);
        entityManager.flush();
        
        // when
        Book foundBook = bookRepository.findById(book.getId()).get();
        foundBook.setTitle("Updated Title");
        foundBook.setAuthor("Updated Author");
        bookRepository.save(foundBook);
        
        // then
        Book updatedBook = bookRepository.findById(book.getId()).get();
        assertThat(updatedBook.getTitle()).isEqualTo("Updated Title");
        assertThat(updatedBook.getAuthor()).isEqualTo("Updated Author");
        assertThat(updatedBook.getIsbn()).isEqualTo("1122334455"); // ISBN remains the same
    }

    @Test
    void shouldDeleteBook() {
        // given
        Book book = new Book();
        book.setTitle("Book to Delete");
        book.setAuthor("Delete Author");
        book.setIsbn("9999999999");
        book.setAvailable(true);
        book.setTotalCopies(1);
        book.setAvailableCopies(1);
        
        entityManager.persist(book);
        entityManager.flush();
        
        // when
        bookRepository.deleteById(book.getId());
        
        // then
        Optional<Book> deletedBook = bookRepository.findById(book.getId());
        assertThat(deletedBook).isEmpty();
    }
}