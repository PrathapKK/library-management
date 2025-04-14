package com.codehawk.library.librarymanagement.integration;

import com.codehawk.library.librarymanagement.model.Book;
import com.codehawk.library.librarymanagement.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BookIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Book testBook;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();

        testBook = new Book();
        testBook.setTitle("Integration Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("9876543210");
        testBook.setPublisher("Test Publisher");
        testBook.setPublicationDate(LocalDate.now());
        testBook.setCategory("Test Category");
        testBook.setTotalCopies(5);
        testBook.setAvailableCopies(5);
        testBook.setAvailable(true);

        testBook = bookRepository.save(testBook);
    }

    @AfterEach
    void tearDown() {
        bookRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetAllBooks() throws Exception {
        // given: books are in DB from setUp

        // when
        ResultActions response = mockMvc.perform(get("/api/books")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is(testBook.getTitle())))
                .andExpect(jsonPath("$[0].author", is(testBook.getAuthor())))
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetBookById() throws Exception {
        // given: book is in DB from setUp

        // when
        ResultActions response = mockMvc.perform(get("/api/books/{id}", testBook.getId())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testBook.getId()))
                .andExpect(jsonPath("$.title", is(testBook.getTitle())))
                .andExpect(jsonPath("$.author", is(testBook.getAuthor())))
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetBookByIsbn() throws Exception {
        // given: book is in DB from setUp

        // when
        ResultActions response = mockMvc.perform(get("/api/books/isbn/{isbn}", testBook.getIsbn())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testBook.getId()))
                .andExpect(jsonPath("$.isbn", is(testBook.getIsbn())))
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetBooksByTitle() throws Exception {
        // given: book is in DB from setUp

        // when
        ResultActions response = mockMvc.perform(get("/api/books/search/title")
                .param("title", "Integration")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is(testBook.getTitle())))
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void shouldAddBook() throws Exception {
        // given
        Book newBook = new Book();
        newBook.setTitle("New Book");
        newBook.setAuthor("New Author");
        newBook.setIsbn("1122334455");
        newBook.setPublisher("New Publisher");
        newBook.setPublicationDate(LocalDate.now());
        newBook.setCategory("New Category");
        newBook.setTotalCopies(3);
        newBook.setAvailableCopies(3);
        newBook.setAvailable(true);

        // when
        ResultActions response = mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newBook)));

        // then
        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is(newBook.getTitle())))
                .andExpect(jsonPath("$.author", is(newBook.getAuthor())))
                .andExpect(jsonPath("$.isbn", is(newBook.getIsbn())))
                .andDo(print());

        // Verify book is saved in DB
        List<Book> books = bookRepository.findAll();
        assertThat(books).hasSize(2);
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void shouldUpdateBook() throws Exception {
        // given
        Book updatedBook = new Book();
        updatedBook.setTitle("Updated Title");
        updatedBook.setAuthor("Updated Author");
        updatedBook.setIsbn(testBook.getIsbn()); // Keep same ISBN
        updatedBook.setPublisher("Updated Publisher");
        updatedBook.setPublicationDate(LocalDate.now());
        updatedBook.setCategory("Updated Category");
        updatedBook.setTotalCopies(10);
        updatedBook.setAvailableCopies(10);
        updatedBook.setAvailable(true);

        // when
        ResultActions response = mockMvc.perform(put("/api/books/{id}", testBook.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedBook)));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testBook.getId()))
                .andExpect(jsonPath("$.title", is(updatedBook.getTitle())))
                .andExpect(jsonPath("$.author", is(updatedBook.getAuthor())))
                .andDo(print());

        // Verify book is updated in DB
        Book updatedBookInDb = bookRepository.findById(testBook.getId()).orElseThrow();
        assertThat(updatedBookInDb.getTitle()).isEqualTo(updatedBook.getTitle());
        assertThat(updatedBookInDb.getAuthor()).isEqualTo(updatedBook.getAuthor());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteBook() throws Exception {
        // given: book is in DB from setUp

        // when
        ResultActions response = mockMvc.perform(delete("/api/books/{id}", testBook.getId())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isNoContent())
                .andDo(print());

        // Verify book is deleted from DB
        List<Book> books = bookRepository.findAll();
        assertThat(books).isEmpty();
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetAvailableBooks() throws Exception {
        // given
        Book unavailableBook = new Book();
        unavailableBook.setTitle("Unavailable Book");
        unavailableBook.setAuthor("Some Author");
        unavailableBook.setIsbn("1122334455");
        unavailableBook.setTotalCopies(1);
        unavailableBook.setAvailableCopies(0);
        unavailableBook.setAvailable(false);
        bookRepository.save(unavailableBook);

        // when
        ResultActions response = mockMvc.perform(get("/api/books/available")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is(testBook.getTitle())))
                .andExpect(jsonPath("$[0].available", is(true)))
                .andDo(print());
    }

    @Test
    void shouldReturnUnauthorizedForUnauthenticatedUser() throws Exception {
        // when - no authentication
        ResultActions response = mockMvc.perform(get("/api/books")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForUnauthorizedRole() throws Exception {
        // given
        Book newBook = new Book();
        newBook.setTitle("New Book");
        newBook.setAuthor("New Author");
        newBook.setIsbn("1122334455");
        newBook.setTotalCopies(3);

        // when - USER role attempting LIBRARIAN operation
        ResultActions response = mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newBook)));

        // then
        response.andExpect(status().isForbidden())
                .andDo(print());
    }
}