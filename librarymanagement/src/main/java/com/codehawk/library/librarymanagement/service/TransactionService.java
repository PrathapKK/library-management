//src/main/java/com/library/management/service/TransactionService.java
package com.codehawk.library.librarymanagement.service;

import com.codehawk.library.librarymanagement.exception.ResourceNotFoundException;
import com.codehawk.library.librarymanagement.model.Book;
import com.codehawk.library.librarymanagement.model.Transaction;
import com.codehawk.library.librarymanagement.model.User;
import com.codehawk.library.librarymanagement.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TransactionService {
 
 private final TransactionRepository transactionRepository;
 private final BookService bookService;
 private final UserService userService;
 
 @Value("${library.max-books-per-user:5}")
 private int maxBooksPerUser;
 
 @Value("${library.loan-period-days:14}")
 private int loanPeriodDays;
 
 @Value("${library.fine-per-day:0.50}")
 private double finePerDay;
 
 @Autowired
 public TransactionService(TransactionRepository transactionRepository, 
                          BookService bookService, 
                          UserService userService) {
     this.transactionRepository = transactionRepository;
     this.bookService = bookService;
     this.userService = userService;
 }
 
 public List<Transaction> getAllTransactions() {
     return transactionRepository.findAll();
 }
 
 public Transaction getTransactionById(Long id) {
     return transactionRepository.findById(id)
             .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
 }
 
 public List<Transaction> getTransactionsByUser(Long userId) {
     User user = userService.getUserById(userId);
     return transactionRepository.findByUser(user);
 }
 
 public List<Transaction> getActiveTransactionsByUser(Long userId) {
     User user = userService.getUserById(userId);
     return transactionRepository.findByUserAndStatus(user, Transaction.Status.ISSUED);
 }
 
 @Transactional
 public Transaction requestBook(Long userId, Long bookId) {
     User user = userService.getUserById(userId);
     Book book = bookService.getBookById(bookId);
     
     // Check if book is available
     if (!book.isAvailable()) {
         throw new IllegalStateException("Book is not available for borrowing");
     }
     
     // Check if user has reached maximum limit
     long activeBooks = transactionRepository.findByUserAndStatus(user, Transaction.Status.ISSUED).size();
     if (activeBooks >= maxBooksPerUser) {
         throw new IllegalStateException("User has reached maximum book limit");
     }
     
     // Create a new transaction with REQUESTED status
     Transaction transaction = new Transaction();
     transaction.setUser(user);
     transaction.setBook(book);
     transaction.setIssueDate(LocalDate.now());
     transaction.setStatus(Transaction.Status.REQUESTED);
     
     return transactionRepository.save(transaction);
 }
 
 @Transactional
 public Transaction issueBook(Long transactionId) {
     Transaction transaction = getTransactionById(transactionId);
     
     // Check if transaction is in REQUESTED state
     if (transaction.getStatus() != Transaction.Status.REQUESTED) {
         throw new IllegalStateException("Transaction is not in REQUESTED state");
     }
     
     // Check if book is still available
     Book book = transaction.getBook();
     if (!book.isAvailable()) {
         throw new IllegalStateException("Book is no longer available");
     }
     
     // Set issue date and due date
     transaction.setIssueDate(LocalDate.now());
     transaction.setDueDate(LocalDate.now().plusDays(loanPeriodDays));
     transaction.setStatus(Transaction.Status.ISSUED);
     
     // Update book availability
     bookService.updateBookAvailability(book.getId(), false);
     
     return transactionRepository.save(transaction);
 }
 
 @Transactional
 public Transaction returnBook(Long transactionId) {
     Transaction transaction = getTransactionById(transactionId);
     
     // Check if transaction is in ISSUED or OVERDUE state
     if (transaction.getStatus() != Transaction.Status.ISSUED && 
         transaction.getStatus() != Transaction.Status.OVERDUE) {
         throw new IllegalStateException("Book is not currently issued");
     }
     
     // Set return date and calculate fine if any
     LocalDate returnDate = LocalDate.now();
     transaction.setReturnDate(returnDate);
     transaction.setStatus(Transaction.Status.RETURNED);
     
     // Calculate fine if returned after due date
     if (returnDate.isAfter(transaction.getDueDate())) {
         long daysLate = ChronoUnit.DAYS.between(transaction.getDueDate(), returnDate);
         transaction.setFine(daysLate * finePerDay);
     } else {
         transaction.setFine(0.0);
     }
     
     // Update book availability
     bookService.updateBookAvailability(transaction.getBook().getId(), true);
     
     return transactionRepository.save(transaction);
 }
 
 public List<Transaction> getOverdueTransactions() {
     return transactionRepository.findByDueDateBeforeAndStatus(
         LocalDate.now(), Transaction.Status.ISSUED);
 }
 
 @Transactional
 public void updateOverdueStatus() {
     List<Transaction> overdueTransactions = transactionRepository.findByDueDateBeforeAndStatus(
         LocalDate.now(), Transaction.Status.ISSUED);
     
     for (Transaction transaction : overdueTransactions) {
         transaction.setStatus(Transaction.Status.OVERDUE);
         transactionRepository.save(transaction);
     }
 }
 
 public double calculateFine(Long transactionId) {
     Transaction transaction = getTransactionById(transactionId);
     
     if (transaction.getStatus() == Transaction.Status.RETURNED) {
         return transaction.getFine();
     }
     
     if (transaction.getDueDate() == null || 
         !LocalDate.now().isAfter(transaction.getDueDate())) {
         return 0.0;
     }
     
     long daysLate = ChronoUnit.DAYS.between(transaction.getDueDate(), LocalDate.now());
     return daysLate * finePerDay;
 }
}