
//src/main/java/com/library/management/service/BookService.java
package com.codehawk.library.librarymanagement.service;

import com.codehawk.library.librarymanagement.exception.ResourceNotFoundException;
import com.codehawk.library.librarymanagement.model.Book;
import com.codehawk.library.librarymanagement.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {
 
 private final BookRepository bookRepository;
 
 @Autowired
 public BookService(BookRepository bookRepository) {
     this.bookRepository = bookRepository;
 }
 
 public List<Book> getAllBooks() {
     return bookRepository.findAll();
 }
 
 public Book getBookById(Long id) {
     return bookRepository.findById(id)
             .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
 }
 
 public Book getBookByIsbn(String isbn) {
     return bookRepository.findByIsbn(isbn)
             .orElseThrow(() -> new ResourceNotFoundException("Book not found with ISBN: " + isbn));
 }
 
 public List<Book> getBooksByTitle(String title) {
     return bookRepository.findByTitleContainingIgnoreCase(title);
 }
 
 public List<Book> getBooksByAuthor(String author) {
     return bookRepository.findByAuthorContainingIgnoreCase(author);
 }
 
 public List<Book> getBooksByCategory(String category) {
     return bookRepository.findByCategoryContainingIgnoreCase(category);
 }
 
 public List<Book> getAvailableBooks() {
     return bookRepository.findByAvailable(true);
 }
 
 public Book addBook(Book book) {
     // Set the available copies equal to total copies when adding a new book
     book.setAvailableCopies(book.getTotalCopies());
     book.setAvailable(book.getAvailableCopies() > 0);
     return bookRepository.save(book);
 }
 
 public Book updateBook(Long id, Book bookDetails) {
     Book book = getBookById(id);
     
     book.setTitle(bookDetails.getTitle());
     book.setAuthor(bookDetails.getAuthor());
     book.setIsbn(bookDetails.getIsbn());
     book.setPublisher(bookDetails.getPublisher());
     book.setPublicationDate(bookDetails.getPublicationDate());
     book.setCategory(bookDetails.getCategory());
     book.setTotalCopies(bookDetails.getTotalCopies());
     
     // Recalculate available copies and availability
     if (bookDetails.getTotalCopies() < book.getTotalCopies() - book.getAvailableCopies()) {
         throw new IllegalArgumentException("Cannot reduce total copies below current borrowed count");
     }
     
     int borrowedCopies = book.getTotalCopies() - book.getAvailableCopies();
     book.setAvailableCopies(bookDetails.getTotalCopies() - borrowedCopies);
     book.setAvailable(book.getAvailableCopies() > 0);
     
     return bookRepository.save(book);
 }
 
 public void deleteBook(Long id) {
     Book book = getBookById(id);
     if (book.getTotalCopies() != book.getAvailableCopies()) {
         throw new IllegalStateException("Cannot delete book as some copies are currently borrowed");
     }
     bookRepository.delete(book);
 }
 
 public void updateBookAvailability(Long id, boolean increase) {
     Book book = getBookById(id);
     if (increase) {
         book.setAvailableCopies(book.getAvailableCopies() + 1);
     } else {
         if (book.getAvailableCopies() <= 0) {
             throw new IllegalStateException("No available copies to borrow");
         }
         book.setAvailableCopies(book.getAvailableCopies() - 1);
     }
     book.setAvailable(book.getAvailableCopies() > 0);
     bookRepository.save(book);
 }
}