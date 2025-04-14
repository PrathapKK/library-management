package com.codehawk.library.librarymanagement.integration;

import com.codehawk.library.librarymanagement.model.Book;
import com.codehawk.library.librarymanagement.model.Transaction;
import com.codehawk.library.librarymanagement.model.User;
import com.codehawk.library.librarymanagement.repository.BookRepository;
import com.codehawk.library.librarymanagement.repository.TransactionRepository;
import com.codehawk.library.librarymanagement.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TransactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private User testLibrarian;
    private Book testBook;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        // Clean up repositories
        transactionRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("user@example.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setRole(User.Role.USER);
        testUser = userRepository.save(testUser);

        // Create test librarian
        testLibrarian = new User();
        testLibrarian.setName("Test Librarian");
        testLibrarian.setEmail("librarian@example.com");
        testLibrarian.setPassword(passwordEncoder.encode("password"));
        testLibrarian.setRole(User.Role.LIBRARIAN);
        testLibrarian = userRepository.save(testLibrarian);

        // Create test book
        testBook = new Book();
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("1234567890");
        testBook.setPublisher("Test Publisher");
        testBook.setPublicationDate(LocalDate.now());
        testBook.setCategory("Test Category");
        testBook.setTotalCopies(3);
        testBook.setAvailableCopies(3);
        testBook.setAvailable(true);
        testBook = bookRepository.save(testBook);

        // Create test transaction
        testTransaction = new Transaction();
        testTransaction.setUser(testUser);
        testTransaction.setBook(testBook);
        testTransaction.setIssueDate(LocalDate.now());
        testTransaction.setDueDate(LocalDate.now().plusDays(14));
        testTransaction.setStatus(Transaction.Status.ISSUED);
        testTransaction = transactionRepository.save(testTransaction);

        // Update book availability after issuing
        testBook.setAvailableCopies(testBook.getAvailableCopies() - 1);
        if (testBook.getAvailableCopies() == 0) {
            testBook.setAvailable(false);
        }
        bookRepository.save(testBook);
    }

    @AfterEach
    void tearDown() {
        transactionRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "ADMIN"})
    void shouldGetAllTransactions() throws Exception {
        // given: transaction is in DB from setUp

        // when
        ResultActions response = mockMvc.perform(get("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testTransaction.getId()))
                .andExpect(jsonPath("$[0].status", is(testTransaction.getStatus().toString())))
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "ADMIN"})
    void shouldGetTransactionById() throws Exception {
        // given: transaction is in DB from setUp

        // when
        ResultActions response = mockMvc.perform(get("/api/transactions/{id}", testTransaction.getId())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTransaction.getId()))
                .andExpect(jsonPath("$.status", is(testTransaction.getStatus().toString())))
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "ADMIN"})
    void shouldGetTransactionsByUser() throws Exception {
        // given: transaction is in DB from setUp

        // when
        ResultActions response = mockMvc.perform(get("/api/transactions/user/{userId}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testTransaction.getId()))
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "ADMIN"})
    void shouldGetActiveTransactionsByUser() throws Exception {
        // given: transaction is in DB from setUp with ISSUED status

        // when
        ResultActions response = mockMvc.perform(get("/api/transactions/user/{userId}/active", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testTransaction.getId()))
                .andExpect(jsonPath("$[0].status", is("ISSUED")))
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "ADMIN"})
    void shouldRequestBook() throws Exception {
        // given
        // Create an additional book for this test
        Book newBook = new Book();
        newBook.setTitle("Another Book");
        newBook.setAuthor("Another Author");
        newBook.setIsbn("9876543210");
        newBook.setPublisher("Test Publisher");
        newBook.setTotalCopies(1);
        newBook.setAvailableCopies(1);
        newBook.setAvailable(true);
        newBook = bookRepository.save(newBook);

        // when
        ResultActions response = mockMvc.perform(post("/api/transactions/request")
                .param("userId", String.valueOf(testUser.getId()))
                .param("bookId", String.valueOf(newBook.getId()))
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("REQUESTED")))
                .andExpect(jsonPath("$.book.id").value(newBook.getId()))
                .andExpect(jsonPath("$.user.id").value(testUser.getId()))
                .andDo(print());

        // Verify transaction is saved in DB
        List<Transaction> transactions = transactionRepository.findByUser(testUser);
        assertThat(transactions).hasSize(2); // Initial + new request
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "ADMIN"})
    void shouldIssueBook() throws Exception {
        // given
        // Create a new REQUESTED transaction
        Book newBook = new Book();
        newBook.setTitle("Book to Issue");
        newBook.setAuthor("Test Author");
        newBook.setIsbn("1122334455");
        newBook.setTotalCopies(1);
        newBook.setAvailableCopies(1);
        newBook.setAvailable(true);
        newBook = bookRepository.save(newBook);

        Transaction requestedTransaction = new Transaction();
        requestedTransaction.setUser(testUser);
        requestedTransaction.setBook(newBook);
        requestedTransaction.setIssueDate(LocalDate.now());
        requestedTransaction.setStatus(Transaction.Status.REQUESTED);
        requestedTransaction = transactionRepository.save(requestedTransaction);

        // when
        ResultActions response = mockMvc.perform(put("/api/transactions/{id}/issue", requestedTransaction.getId())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestedTransaction.getId()))
                .andExpect(jsonPath("$.status", is("ISSUED")))
                .andExpect(jsonPath("$.dueDate", notNullValue()))
                .andDo(print());

        // Verify transaction status is updated in DB
        Transaction updatedTransaction = transactionRepository.findById(requestedTransaction.getId()).orElseThrow();
        assertThat(updatedTransaction.getStatus()).isEqualTo(Transaction.Status.ISSUED);
        assertThat(updatedTransaction.getDueDate()).isNotNull();

        // Verify book availability is updated
        Book updatedBook = bookRepository.findById(newBook.getId()).orElseThrow();
        assertThat(updatedBook.getAvailableCopies()).isEqualTo(0);
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "ADMIN"})
    void shouldReturnBook() throws Exception {
        // given: transaction is in DB from setUp with ISSUED status

        // when
        ResultActions response = mockMvc.perform(put("/api/transactions/{id}/return", testTransaction.getId())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTransaction.getId()))
                .andExpect(jsonPath("$.status", is("RETURNED")))
                .andExpect(jsonPath("$.returnDate", notNullValue()))
                .andDo(print());

        // Verify transaction status is updated in DB
        Transaction updatedTransaction = transactionRepository.findById(testTransaction.getId()).orElseThrow();
        assertThat(updatedTransaction.getStatus()).isEqualTo(Transaction.Status.RETURNED);
        assertThat(updatedTransaction.getReturnDate()).isNotNull();

        // Verify book availability is updated
        Book updatedBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assertThat(updatedBook.getAvailableCopies()).isEqualTo(testBook.getAvailableCopies() + 1);
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "ADMIN"})
    void shouldCalculateFineForOverdueTransaction() throws Exception {
        // given
        // Create an overdue transaction
        Transaction overdueTransaction = new Transaction();
        overdueTransaction.setUser(testUser);
        overdueTransaction.setBook(testBook);
        overdueTransaction.setIssueDate(LocalDate.now().minusDays(30));
        overdueTransaction.setDueDate(LocalDate.now().minusDays(16)); // 16 days overdue
        overdueTransaction.setStatus(Transaction.Status.OVERDUE);
        overdueTransaction = transactionRepository.save(overdueTransaction);

        // when
        ResultActions response = mockMvc.perform(get("/api/transactions/{id}/fine", overdueTransaction.getId())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.any(String.class)))
                .andDo(print());

        // The exact fine amount depends on your configuration, but it should be positive
        double fine = Double.parseDouble(response.andReturn().getResponse().getContentAsString());
        assertThat(fine).isGreaterThan(0.0);
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "ADMIN"})
    void shouldUpdateOverdueStatus() throws Exception {
        // given
        // Create a transaction that's past due but still in ISSUED status
        Transaction pastDueTransaction = new Transaction();
        pastDueTransaction.setUser(testUser);
        pastDueTransaction.setBook(testBook);
        pastDueTransaction.setIssueDate(LocalDate.now().minusDays(20));
        pastDueTransaction.setDueDate(LocalDate.now().minusDays(6)); // 6 days overdue
        pastDueTransaction.setStatus(Transaction.Status.ISSUED);
        pastDueTransaction = transactionRepository.save(pastDueTransaction);

        // when
        ResultActions response = mockMvc.perform(put("/api/transactions/update-overdue")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isOk())
                .andDo(print());

        // Verify transaction status is updated to OVERDUE
        Transaction updatedTransaction = transactionRepository.findById(pastDueTransaction.getId()).orElseThrow();
        assertThat(updatedTransaction.getStatus()).isEqualTo(Transaction.Status.OVERDUE);
    }

    @Test
    void shouldReturnUnauthorizedForUnauthenticatedUser() throws Exception {
        // when - no authentication
        ResultActions response = mockMvc.perform(get("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForUnauthorizedRole() throws Exception {
        // when - USER role attempting LIBRARIAN operation
        ResultActions response = mockMvc.perform(get("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "ADMIN"})
    void shouldGetOverdueTransactions() throws Exception {
        // given
        // Create an overdue transaction
        Transaction overdueTransaction = new Transaction();
        overdueTransaction.setUser(testUser);
        overdueTransaction.setBook(testBook);
        overdueTransaction.setIssueDate(LocalDate.now().minusDays(20));
        overdueTransaction.setDueDate(LocalDate.now().minusDays(6));
        overdueTransaction.setStatus(Transaction.Status.OVERDUE);
        overdueTransaction = transactionRepository.save(overdueTransaction);

        // when
        ResultActions response = mockMvc.perform(get("/api/transactions/overdue")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("OVERDUE")))
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "ADMIN"})
    void shouldHandleRequestForUnavailableBook() throws Exception {
        // given
        // Make the book unavailable
        testBook.setAvailableCopies(0);
        testBook.setAvailable(false);
        bookRepository.save(testBook);

        // when
        ResultActions response = mockMvc.perform(post("/api/transactions/request")
                .param("userId", String.valueOf(testUser.getId()))
                .param("bookId", String.valueOf(testBook.getId()))
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "ADMIN"})
    void shouldHandleMaxBooksReachedDuringRequest() throws Exception {
        // given
        // Setup max books limit (typically in application properties)
        // For this test, we'll create transactions to reach the limit
        // Assuming max limit is 5 books
        
        // Create 4 more books (plus 1 from setUp = 5 total)
        for (int i = 0; i < 4; i++) {
            Book book = new Book();
            book.setTitle("Book " + i);
            book.setAuthor("Author " + i);
            book.setIsbn("ISBN" + i);
            book.setTotalCopies(1);
            book.setAvailableCopies(1);
            book.setAvailable(true);
            book = bookRepository.save(book);
            
            Transaction transaction = new Transaction();
            transaction.setUser(testUser);
            transaction.setBook(book);
            transaction.setIssueDate(LocalDate.now());
            transaction.setDueDate(LocalDate.now().plusDays(14));
            transaction.setStatus(Transaction.Status.ISSUED);
            transactionRepository.save(transaction);
        }
        
        // Create one more book for the request that should fail
        Book extraBook = new Book();
        extraBook.setTitle("Extra Book");
        extraBook.setAuthor("Extra Author");
        extraBook.setIsbn("ISBN-extra");
        extraBook.setTotalCopies(1);
        extraBook.setAvailableCopies(1);
        extraBook.setAvailable(true);
        extraBook = bookRepository.save(extraBook);

        // when
        ResultActions response = mockMvc.perform(post("/api/transactions/request")
                .param("userId", String.valueOf(testUser.getId()))
                .param("bookId", String.valueOf(extraBook.getId()))
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isBadRequest())
                .andDo(print());
    }
}