
package com.codehawk.library.librarymanagement.service;

import com.codehawk.library.librarymanagement.exception.ResourceNotFoundException;
import com.codehawk.library.librarymanagement.model.Book;
import com.codehawk.library.librarymanagement.model.Transaction;
import com.codehawk.library.librarymanagement.model.User;
import com.codehawk.library.librarymanagement.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BookService bookService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Book book;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        
        book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setIsbn("1234567890");
        book.setAvailable(true);
        book.setTotalCopies(5);
        book.setAvailableCopies(3);
        
        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setUser(user);
        transaction.setBook(book);
        transaction.setIssueDate(LocalDate.now());
        transaction.setStatus(Transaction.Status.REQUESTED);
        
        // Set default values for properties
        ReflectionTestUtils.setField(transactionService, "maxBooksPerUser", 5);
        ReflectionTestUtils.setField(transactionService, "loanPeriodDays", 14);
        ReflectionTestUtils.setField(transactionService, "finePerDay", 0.50);
    }

    @Test
    void shouldGetAllTransactions() {
        // given
        List<Transaction> transactions = Arrays.asList(transaction);
        given(transactionRepository.findAll()).willReturn(transactions);

        // when
        List<Transaction> result = transactionService.getAllTransactions();

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void shouldGetTransactionById() {
        // given
        given(transactionRepository.findById(1L)).willReturn(Optional.of(transaction));

        // when
        Transaction result = transactionService.getTransactionById(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void shouldThrowExceptionWhenTransactionNotFound() {
        // given
        given(transactionRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.getTransactionById(1L);
        });
    }

    @Test
    void shouldGetTransactionsByUser() {
        // given
        List<Transaction> transactions = Arrays.asList(transaction);
        given(userService.getUserById(1L)).willReturn(user);
        given(transactionRepository.findByUser(user)).willReturn(transactions);

        // when
        List<Transaction> result = transactionService.getTransactionsByUser(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void shouldGetActiveTransactionsByUser() {
        // given
        List<Transaction> transactions = Arrays.asList(transaction);
        given(userService.getUserById(1L)).willReturn(user);
        given(transactionRepository.findByUserAndStatus(user, Transaction.Status.ISSUED)).willReturn(transactions);

        // when
        List<Transaction> result = transactionService.getActiveTransactionsByUser(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void shouldRequestBook() {
        // given
        given(userService.getUserById(1L)).willReturn(user);
        given(bookService.getBookById(1L)).willReturn(book);
        given(transactionRepository.findByUserAndStatus(user, Transaction.Status.ISSUED)).willReturn(Collections.emptyList());
        given(transactionRepository.save(any(Transaction.class))).willReturn(transaction);

        // when
        Transaction result = transactionService.requestBook(1L, 1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Transaction.Status.REQUESTED);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void shouldThrowExceptionWhenRequestingUnavailableBook() {
        // given
        Book unavailableBook = new Book();
        unavailableBook.setId(1L);
        unavailableBook.setAvailable(false);
        
        given(userService.getUserById(1L)).willReturn(user);
        given(bookService.getBookById(1L)).willReturn(unavailableBook);

        // when & then
        assertThrows(IllegalStateException.class, () -> {
            transactionService.requestBook(1L, 1L);
        });
        
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void shouldThrowExceptionWhenUserReachedMaxBooks() {
        // given
        List<Transaction> transactions = Arrays.asList(
            new Transaction(), new Transaction(), new Transaction(), new Transaction(), new Transaction()
        );
        
        given(userService.getUserById(1L)).willReturn(user);
        given(bookService.getBookById(1L)).willReturn(book);
        given(transactionRepository.findByUserAndStatus(user, Transaction.Status.ISSUED)).willReturn(transactions);

        // when & then
        assertThrows(IllegalStateException.class, () -> {
            transactionService.requestBook(1L, 1L);
        });
        
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void shouldIssueBook() {
        // given
        Transaction requestedTransaction = new Transaction();
        requestedTransaction.setId(1L);
        requestedTransaction.setUser(user);
        requestedTransaction.setBook(book);
        requestedTransaction.setStatus(Transaction.Status.REQUESTED);
        
        given(transactionRepository.findById(1L)).willReturn(Optional.of(requestedTransaction));
        given(transactionRepository.save(any(Transaction.class))).willReturn(transaction);
        
        // when
        Transaction result = transactionService.issueBook(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Transaction.Status.ISSUED);
        assertThat(result.getDueDate()).isNotNull();
        verify(bookService, times(1)).updateBookAvailability(1L, false);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void shouldThrowExceptionWhenIssuingNonRequestedTransaction() {
        // given
        Transaction issuedTransaction = new Transaction();
        issuedTransaction.setId(1L);
        issuedTransaction.setStatus(Transaction.Status.ISSUED);
        
        given(transactionRepository.findById(1L)).willReturn(Optional.of(issuedTransaction));

        // when & then
        assertThrows(IllegalStateException.class, () -> {
            transactionService.issueBook(1L);
        });
        
        verify(bookService, never()).updateBookAvailability(anyLong(), anyBoolean());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void shouldThrowExceptionWhenIssuingUnavailableBook() {
        // given
        Transaction requestedTransaction = new Transaction();
        requestedTransaction.setId(1L);
        requestedTransaction.setStatus(Transaction.Status.REQUESTED);
        
        Book unavailableBook = new Book();
        unavailableBook.setId(1L);
        unavailableBook.setAvailable(false);
        
        requestedTransaction.setBook(unavailableBook);
        
        given(transactionRepository.findById(1L)).willReturn(Optional.of(requestedTransaction));

        // when & then
        assertThrows(IllegalStateException.class, () -> {
            transactionService.issueBook(1L);
        });
        
        verify(bookService, never()).updateBookAvailability(anyLong(), anyBoolean());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void shouldReturnBook() {
        // given
        Transaction issuedTransaction = new Transaction();
        issuedTransaction.setId(1L);
        issuedTransaction.setUser(user);
        issuedTransaction.setBook(book);
        issuedTransaction.setIssueDate(LocalDate.now().minusDays(7));
        issuedTransaction.setDueDate(LocalDate.now().plusDays(7));
        issuedTransaction.setStatus(Transaction.Status.ISSUED);
        
        given(transactionRepository.findById(1L)).willReturn(Optional.of(issuedTransaction));
        given(transactionRepository.save(any(Transaction.class))).willReturn(transaction);
        
        // when
        Transaction result = transactionService.returnBook(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Transaction.Status.RETURNED);
        assertThat(result.getReturnDate()).isNotNull();
        assertThat(result.getFine()).isEqualTo(0.0); // No fine as returned before due date
        verify(bookService, times(1)).updateBookAvailability(anyLong(), eq(true));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void shouldCalculateFineWhenReturnedLate() {
        // given
        Transaction overdueTransaction = new Transaction();
        overdueTransaction.setId(1L);
        overdueTransaction.setUser(user);
        overdueTransaction.setBook(book);
        overdueTransaction.setIssueDate(LocalDate.now().minusDays(21)); // Issued 21 days ago
        overdueTransaction.setDueDate(LocalDate.now().minusDays(7)); // Due 7 days ago
        overdueTransaction.setStatus(Transaction.Status.OVERDUE);
        
        given(transactionRepository.findById(1L)).willReturn(Optional.of(overdueTransaction));
        given(transactionRepository.save(any(Transaction.class))).willReturn(transaction);
        
        // when
        Transaction result = transactionService.returnBook(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Transaction.Status.RETURNED);
        assertThat(result.getReturnDate()).isNotNull();
        assertThat(result.getFine()).isGreaterThan(0.0); // Should have fine
        verify(bookService, times(1)).updateBookAvailability(anyLong(), eq(true));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void shouldThrowExceptionWhenReturningNonIssuedBook() {
        // given
        Transaction requestedTransaction = new Transaction();
        requestedTransaction.setId(1L);
        requestedTransaction.setStatus(Transaction.Status.REQUESTED);
        
        given(transactionRepository.findById(1L)).willReturn(Optional.of(requestedTransaction));

        // when & then
        assertThrows(IllegalStateException.class, () -> {
            transactionService.returnBook(1L);
        });
        
        verify(bookService, never()).updateBookAvailability(anyLong(), anyBoolean());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void shouldGetOverdueTransactions() {
        // given
        List<Transaction> overdueTransactions = Arrays.asList(transaction);
        given(transactionRepository.findByDueDateBeforeAndStatus(
            any(LocalDate.class), eq(Transaction.Status.ISSUED))).willReturn(overdueTransactions);

        // when
        List<Transaction> result = transactionService.getOverdueTransactions();

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void shouldUpdateOverdueStatus() {
        // given
        Transaction issuedTransaction = new Transaction();
        issuedTransaction.setId(1L);
        issuedTransaction.setStatus(Transaction.Status.ISSUED);
        
        List<Transaction> overdueTransactions = Arrays.asList(issuedTransaction);
        given(transactionRepository.findByDueDateBeforeAndStatus(
            any(LocalDate.class), eq(Transaction.Status.ISSUED))).willReturn(overdueTransactions);
        
        // when
        transactionService.updateOverdueStatus();

        // then
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        assertThat(issuedTransaction.getStatus()).isEqualTo(Transaction.Status.OVERDUE);
    }

    @Test
    void shouldCalculateFineForOverdueTransaction() {
        // given
        Transaction overdueTransaction = new Transaction();
        overdueTransaction.setId(1L);
        overdueTransaction.setDueDate(LocalDate.now().minusDays(5)); // 5 days overdue
        overdueTransaction.setStatus(Transaction.Status.ISSUED);
        
        given(transactionRepository.findById(1L)).willReturn(Optional.of(overdueTransaction));
        double expectedFine = 5 * 0.50; // 5 days * 0.50 per day

        // when
        double actualFine = transactionService.calculateFine(1L);

        // then
        assertThat(actualFine).isEqualTo(expectedFine);
    }

    @Test
    void shouldReturnZeroFineForNonOverdueTransaction() {
        // given
        Transaction nonOverdueTransaction = new Transaction();
        nonOverdueTransaction.setId(1L);
        nonOverdueTransaction.setDueDate(LocalDate.now().plusDays(5)); // Due in 5 days
        nonOverdueTransaction.setStatus(Transaction.Status.ISSUED);
        
        given(transactionRepository.findById(1L)).willReturn(Optional.of(nonOverdueTransaction));

        // when
        double actualFine = transactionService.calculateFine(1L);

        // then
        assertThat(actualFine).isEqualTo(0.0);
    }

    @Test
    void shouldReturnStoredFineForReturnedTransaction() {
        // given
        Transaction returnedTransaction = new Transaction();
        returnedTransaction.setId(1L);
        returnedTransaction.setStatus(Transaction.Status.RETURNED);
        returnedTransaction.setFine(2.50);
        
        given(transactionRepository.findById(1L)).willReturn(Optional.of(returnedTransaction));

        // when
        double actualFine = transactionService.calculateFine(1L);

        // then
        assertThat(actualFine).isEqualTo(2.50);
    }
}