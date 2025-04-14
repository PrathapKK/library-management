
//src/main/java/com/library/management/controller/BookController.java
package com.codehawk.library.librarymanagement.controller;

import com.codehawk.library.librarymanagement.model.Book;
import com.codehawk.library.librarymanagement.service.BookService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {
 
 private final BookService bookService;
 
 @Autowired
 public BookController(BookService bookService) {
     this.bookService = bookService;
 }
 
 @GetMapping
 public ResponseEntity<List<Book>> getAllBooks() {
     return ResponseEntity.ok(bookService.getAllBooks());
 }
 
 @GetMapping("/{id}")
 public ResponseEntity<Book> getBookById(@PathVariable Long id) {
     return ResponseEntity.ok(bookService.getBookById(id));
 }
 
 @GetMapping("/isbn/{isbn}")
 public ResponseEntity<Book> getBookByIsbn(@PathVariable String isbn) {
     return ResponseEntity.ok(bookService.getBookByIsbn(isbn));
 }
 
 @GetMapping("/search/title")
 public ResponseEntity<List<Book>> getBooksByTitle(@RequestParam String title) {
     return ResponseEntity.ok(bookService.getBooksByTitle(title));
 }
 
 @GetMapping("/search/author")
 public ResponseEntity<List<Book>> getBooksByAuthor(@RequestParam String author) {
     return ResponseEntity.ok(bookService.getBooksByAuthor(author));
 }
 
 @GetMapping("/search/category")
 public ResponseEntity<List<Book>> getBooksByCategory(@RequestParam String category) {
     return ResponseEntity.ok(bookService.getBooksByCategory(category));
 }
 
 @GetMapping("/available")
 public ResponseEntity<List<Book>> getAvailableBooks() {
     return ResponseEntity.ok(bookService.getAvailableBooks());
 }
 
 @PostMapping
 public ResponseEntity<Book> addBook(@Valid @RequestBody Book book) {
     return new ResponseEntity<>(bookService.addBook(book), HttpStatus.CREATED);
 }
 
 @PutMapping("/{id}")
 public ResponseEntity<Book> updateBook(@PathVariable Long id, @Valid @RequestBody Book book) {
     return ResponseEntity.ok(bookService.updateBook(id, book));
 }
 
 @DeleteMapping("/{id}")
 public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
     bookService.deleteBook(id);
     return ResponseEntity.noContent().build();
 }
}