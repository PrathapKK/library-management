package com.codehawk.library.librarymanagement.controller;

import com.codehawk.library.librarymanagement.model.Book;
import com.codehawk.library.librarymanagement.model.Transaction;
import com.codehawk.library.librarymanagement.model.User;
import com.codehawk.library.librarymanagement.service.TransactionService;
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

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    private Transaction testTransaction;
    private User testUser;
    private Book testBook;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setRole(User.Role.USER);

        // Setup test book
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("1234567890");
        testBook.setAvailable(true);

        // Setup test transaction
        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setUser(testUser);
        testTransaction.setBook(testBook);
        testTransaction.setIssueDate(LocalDate.now());
        testTransaction.setDueDate(LocalDate.now().plusDays(14));
        testTransaction.setStatus(Transaction.Status.ISSUED);
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "ADMIN"})
    void shouldGetAllTransactions() throws Exception {
        List<Transaction> transactions = Arrays.asList(testTransaction);
        given(transactionService.getAllTransactions()).willReturn(transactions);

        mockMvc.perform(get("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "ADMIN"})
    void shouldGetTransactionById() throws Exception {
        given(transactionService.getTransactionById(1L)).willReturn(testTransaction);

        mockMvc.perform(get("/api/transactions/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("ISSUED")));
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "ADMIN"})
    void shouldGetTransactionsByUser() throws Exception {
        List<Transaction> transactions = Arrays.asList(testTransaction);
        given(transactionService.getTransactionsByUser(1L)).willReturn(transactions);

        mockMvc.perform(get("/api/transactions/user/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "ADMIN"})
    void shouldGetActiveTransactionsByUser() throws Exception {
        List<Transaction> transactions = Arrays.asList(testTransaction);
        given(transactionService.getActiveTransactionsByUser(1L)).willReturn(transactions);

        mockMvc.perform(get("/api/transactions/user/1/active")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("ISSUED")));
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "ADMIN"})
    void shouldGetOverdueTransactions() throws Exception {
        testTransaction.setStatus(Transaction.Status.OVERDUE);
        List<Transaction> transactions = Arrays.asList(testTransaction);
        given(transactionService.getOverdueTransactions()).willReturn(transactions);

        mockMvc.perform(get("/api/transactions/overdue")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("OVERDUE")));
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "ADMIN"})
    void shouldRequestBook() throws Exception {
        given(transactionService.requestBook(anyLong(), anyLong())).willReturn(testTransaction);

        mockMvc.perform(post("/api/transactions/request")
                .param("userId", "1")
                .param("bookId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "ADMIN"})
    void shouldIssueBook() throws Exception {
        testTransaction.setStatus(Transaction.Status.ISSUED);
        given(transactionService.issueBook(anyLong())).willReturn(testTransaction);

        mockMvc.perform(put("/api/transactions/1/issue")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ISSUED")));
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "ADMIN"})
    void shouldReturnBook() throws Exception {
        testTransaction.setStatus(Transaction.Status.RETURNED);
        testTransaction.setReturnDate(LocalDate.now());
        given(transactionService.returnBook(anyLong())).willReturn(testTransaction);

        mockMvc.perform(put("/api/transactions/1/return")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RETURNED")))
                .andExpect(jsonPath("$.returnDate", notNullValue()));
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "ADMIN"})
    void shouldCalculateFine() throws Exception {
        double fine = 2.5;
        given(transactionService.calculateFine(anyLong())).willReturn(fine);

        mockMvc.perform(get("/api/transactions/1/fine")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("2.5"));
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN", "ADMIN"})
    void shouldUpdateOverdueStatus() throws Exception {
        doNothing().when(transactionService).updateOverdueStatus();

        mockMvc.perform(put("/api/transactions/update-overdue")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}