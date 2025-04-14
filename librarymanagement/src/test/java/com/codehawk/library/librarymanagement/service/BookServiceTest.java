
//src/test/java/com/library/management/service/BookServiceTest.java
package com.codehawk.library.librarymanagement.service;

import com.codehawk.library.librarymanagement.exception.ResourceNotFoundException;
import com.codehawk.library.librarymanagement.model.Book;
import com.codehawk.library.librarymanagement.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

 @Mock
 private BookRepository bookRepository;

 @InjectMocks
 private BookService bookService;

 private Book book;

 @BeforeEach
 void setUp() {
     book = new Book();
     book.setId(1L);
     book.setTitle("Test Book");
     book.setAuthor("Test Author");
     book.setIsbn("1234567890");
     book.setPublisher("Test Publisher");
     book.setPublicationDate(LocalDate.now());
     book.setCategory("Test Category");
     book.setTotalCopies(5);
     book.setAvailableCopies(5);
     book.setAvailable(true);
 }

 @Test
 void shouldGetAllBooks() {
     // given
     List<Book> books = Arrays.asList(book);
     given(bookRepository.findAll()).willReturn(books);

     // when
     List<Book> result = bookService.getAllBooks();

     // then
     assertThat(result).isNotNull();
     assertThat(result.size()).isEqualTo(1);
     assertThat(result.get(0).getTitle()).isEqualTo("Test Book");
 }

 @Test
 void shouldGetBookById() {
     // given
     given(bookRepository.findById(1L)).willReturn(Optional.of(book));

     // when
     Book result = bookService.getBookById(1L);

     // then
     assertThat(result).isNotNull();
     assertThat(result.getId()).isEqualTo(1L);
 }

 @Test
 void shouldThrowExceptionWhenBookNotFound() {
     // given
     given(bookRepository.findById(1L)).willReturn(Optional.empty());

     // when & then
     assertThrows(ResourceNotFoundException.class, () -> {
         bookService.getBookById(1L);
     });
 }

 @Test
 void shouldGetBookByIsbn() {
     // given
     given(bookRepository.findByIsbn("1234567890")).willReturn(Optional.of(book));

     // when
     Book result = bookService.getBookByIsbn("1234567890");

     // then
     assertThat(result).isNotNull();
     assertThat(result.getIsbn()).isEqualTo("1234567890");
 }

 @Test
 void shouldThrowExceptionWhenBookNotFoundByIsbn() {
     // given
     given(bookRepository.findByIsbn("1234567890")).willReturn(Optional.empty());

     // when & then
     assertThrows(ResourceNotFoundException.class, () -> {
         bookService.getBookByIsbn("1234567890");
     });
 }

 @Test
 void shouldGetBooksByTitle() {
     // given
     List<Book> books = Arrays.asList(book);
     given(bookRepository.findByTitleContainingIgnoreCase("Test")).willReturn(books);

     // when
     List<Book> result = bookService.getBooksByTitle("Test");

     // then
     assertThat(result).isNotNull();
     assertThat(result.size()).isEqualTo(1);
     assertThat(result.get(0).getTitle()).isEqualTo("Test Book");
 }

 @Test
 void shouldGetBooksByAuthor() {
     // given
     List<Book> books = Arrays.asList(book);
     given(bookRepository.findByAuthorContainingIgnoreCase("Test")).willReturn(books);

     // when
     List<Book> result = bookService.getBooksByAuthor("Test");

     // then
     assertThat(result).isNotNull();
     assertThat(result.size()).isEqualTo(1);
     assertThat(result.get(0).getAuthor()).isEqualTo("Test Author");
 }

 @Test
 void shouldGetBooksByCategory() {
     // given
     List<Book> books = Arrays.asList(book);
     given(bookRepository.findByCategoryContainingIgnoreCase("Test")).willReturn(books);

     // when
     List<Book> result = bookService.getBooksByCategory("Test");

     // then
     assertThat(result).isNotNull();
     assertThat(result.size()).isEqualTo(1);
     assertThat(result.get(0).getCategory()).isEqualTo("Test Category");
 }

 @Test
 void shouldGetAvailableBooks() {
     // given
     List<Book> books = Arrays.asList(book);
     given(bookRepository.findByAvailable(true)).willReturn(books);

     // when
     List<Book> result = bookService.getAvailableBooks();

     // then
     assertThat(result).isNotNull();
     assertThat(result.size()).isEqualTo(1);
     assertThat(result.get(0).isAvailable()).isEqualTo(true);
 }

 @Test
 void shouldAddBook() {
     // given
     given(bookRepository.save(any(Book.class))).willReturn(book);

     // when
     Book savedBook = bookService.addBook(book);

     // then
     assertThat(savedBook).isNotNull();
     assertThat(savedBook.getAvailableCopies()).isEqualTo(savedBook.getTotalCopies());
     assertThat(savedBook.isAvailable()).isEqualTo(true);
     verify(bookRepository, times(1)).save(any(Book.class));
 }

 @Test
 void shouldUpdateBook() {
     // given
     Book updatedBook = new Book();
     updatedBook.setTitle("Updated Title");
     updatedBook.setAuthor("Updated Author");
     updatedBook.setIsbn("1234567890");
     updatedBook.setTotalCopies(5);
     
     given(bookRepository.findById(1L)).willReturn(Optional.of(book));
     given(bookRepository.save(any(Book.class))).willReturn(book);

     // when
     Book result = bookService.updateBook(1L, updatedBook);

     // then
     assertThat(result).isNotNull();
     verify(bookRepository, times(1)).save(any(Book.class));
 }

 @Test
 void shouldThrowExceptionWhenReducingCopiesBelowBorrowed() {
     // given
     Book existingBook = new Book();
     existingBook.setId(1L);
     existingBook.setTotalCopies(5);
     existingBook.setAvailableCopies(2); // 3 copies are borrowed
     
     Book updatedBook = new Book();
     updatedBook.setTotalCopies(2); // Trying to reduce to 2 copies
     
     given(bookRepository.findById(1L)).willReturn(Optional.of(existingBook));

     // when & then
     assertThrows(IllegalArgumentException.class, () -> {
         bookService.updateBook(1L, updatedBook);
     });
     
     verify(bookRepository, never()).save(any(Book.class));
 }

 @Test
 void shouldDeleteBook() {
     // given
     given(bookRepository.findById(1L)).willReturn(Optional.of(book));
     
     // when
     bookService.deleteBook(1L);
     
     // then
     verify(bookRepository, times(1)).delete(any(Book.class));
 }
 
 @Test
 void shouldThrowExceptionWhenDeletingBorrowedBook() {
     // given
     Book borrowedBook = new Book();
     borrowedBook.setId(1L);
     borrowedBook.setTotalCopies(5);
     borrowedBook.setAvailableCopies(3); // 2 copies are borrowed
     
     given(bookRepository.findById(1L)).willReturn(Optional.of(borrowedBook));

     // when & then
     assertThrows(IllegalStateException.class, () -> {
         bookService.deleteBook(1L);
     });
     
     verify(bookRepository, never()).delete(any(Book.class));
 }

 @Test
 void shouldUpdateBookAvailabilityWhenIncreasing() {
     // given
     given(bookRepository.findById(1L)).willReturn(Optional.of(book));
     given(bookRepository.save(any(Book.class))).willReturn(book);

     // when
     bookService.updateBookAvailability(1L, true);

     // then
     assertThat(book.getAvailableCopies()).isEqualTo(6);
     assertThat(book.isAvailable()).isEqualTo(true);
     verify(bookRepository, times(1)).save(any(Book.class));
 }

 @Test
 void shouldUpdateBookAvailabilityWhenDecreasing() {
     // given
     given(bookRepository.findById(1L)).willReturn(Optional.of(book));
     given(bookRepository.save(any(Book.class))).willReturn(book);

     // when
     bookService.updateBookAvailability(1L, false);

     // then
     assertThat(book.getAvailableCopies()).isEqualTo(4);
     assertThat(book.isAvailable()).isEqualTo(true);
     verify(bookRepository, times(1)).save(any(Book.class));
 }

 @Test
 void shouldThrowExceptionWhenDecreasingAvailableCopiesBelowZero() {
     // given
     Book noAvailableCopiesBook = new Book();
     noAvailableCopiesBook.setId(1L);
     noAvailableCopiesBook.setAvailableCopies(0);
     
     given(bookRepository.findById(1L)).willReturn(Optional.of(noAvailableCopiesBook));

     // when & then
     assertThrows(IllegalStateException.class, () -> {
         bookService.updateBookAvailability(1L, false);
     });
     
     verify(bookRepository, never()).save(any(Book.class));
 }
}