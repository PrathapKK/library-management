package com.codehawk.library.librarymanagement.controller;

import com.codehawk.library.librarymanagement.model.Book;
import com.codehawk.library.librarymanagement.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    private Book testBook;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("1234567890");
        testBook.setPublisher("Test Publisher");
        testBook.setPublicationDate(LocalDate.now());
        testBook.setCategory("Test Category");
        testBook.setTotalCopies(5);
        testBook.setAvailableCopies(5);
        testBook.setAvailable(true);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetAllBooks() throws Exception {
        List<Book> books = Arrays.asList(testBook);
        given(bookService.getAllBooks()).willReturn(books);

        mockMvc.perform(get("/api/books")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Test Book")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetBookById() throws Exception {
        given(bookService.getBookById(1L)).willReturn(testBook);

        mockMvc.perform(get("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Book")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetBookByIsbn() throws Exception {
        given(bookService.getBookByIsbn("1234567890")).willReturn(testBook);

        mockMvc.perform(get("/api/books/isbn/1234567890")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn", is("1234567890")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetBooksByTitle() throws Exception {
        List<Book> books = Arrays.asList(testBook);
        given(bookService.getBooksByTitle("Test")).willReturn(books);

        mockMvc.perform(get("/api/books/search/title")
                .param("title", "Test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Test Book")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetBooksByAuthor() throws Exception {
        List<Book> books = Arrays.asList(testBook);
        given(bookService.getBooksByAuthor("Test")).willReturn(books);

        mockMvc.perform(get("/api/books/search/author")
                .param("author", "Test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].author", is("Test Author")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetBooksByCategory() throws Exception {
        List<Book> books = Arrays.asList(testBook);
        given(bookService.getBooksByCategory("Test")).willReturn(books);

        mockMvc.perform(get("/api/books/search/category")
                .param("category", "Test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category", is("Test Category")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetAvailableBooks() throws Exception {
        List<Book> books = Arrays.asList(testBook);
        given(bookService.getAvailableBooks()).willReturn(books);

        mockMvc.perform(get("/api/books/available")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].available", is(true)));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void shouldAddBook() throws Exception {
        given(bookService.addBook(any(Book.class))).willReturn(testBook);

        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBook)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Test Book")));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void shouldUpdateBook() throws Exception {
        Book updatedBook = new Book();
        updatedBook.setId(1L);
        updatedBook.setTitle("Updated Book");
        updatedBook.setAuthor("Updated Author");
        updatedBook.setIsbn("1234567890");
        updatedBook.setTotalCopies(5);
        
        given(bookService.updateBook(anyLong(), any(Book.class))).willReturn(updatedBook);

        mockMvc.perform(put("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedBook)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Book")))
                .andExpect(jsonPath("$.author", is("Updated Author")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteBook() throws Exception {
        doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}