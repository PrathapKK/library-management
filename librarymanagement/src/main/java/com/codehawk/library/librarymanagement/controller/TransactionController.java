
//src/main/java/com/library/management/controller/TransactionController.java
package com.codehawk.library.librarymanagement.controller;

import com.codehawk.library.librarymanagement.model.Transaction;
import com.codehawk.library.librarymanagement.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
 
 private final TransactionService transactionService;
 
 @Autowired
 public TransactionController(TransactionService transactionService) {
     this.transactionService = transactionService;
 }
 
 @GetMapping
 public ResponseEntity<List<Transaction>> getAllTransactions() {
     return ResponseEntity.ok(transactionService.getAllTransactions());
 }
 
 @GetMapping("/{id}")
 public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
     return ResponseEntity.ok(transactionService.getTransactionById(id));
 }
 
 @GetMapping("/user/{userId}")
 public ResponseEntity<List<Transaction>> getTransactionsByUser(@PathVariable Long userId) {
     return ResponseEntity.ok(transactionService.getTransactionsByUser(userId));
 }
 
 @GetMapping("/user/{userId}/active")
 public ResponseEntity<List<Transaction>> getActiveTransactionsByUser(@PathVariable Long userId) {
     return ResponseEntity.ok(transactionService.getActiveTransactionsByUser(userId));
 }
 
 @GetMapping("/overdue")
 public ResponseEntity<List<Transaction>> getOverdueTransactions() {
     return ResponseEntity.ok(transactionService.getOverdueTransactions());
 }
 
 @PostMapping("/request")
 public ResponseEntity<Transaction> requestBook(
         @RequestParam Long userId,
         @RequestParam Long bookId) {
     return new ResponseEntity<>(transactionService.requestBook(userId, bookId), HttpStatus.CREATED);
 }
 
 @PutMapping("/{id}/issue")
 public ResponseEntity<Transaction> issueBook(@PathVariable Long id) {
     return ResponseEntity.ok(transactionService.issueBook(id));
 }
 
 @PutMapping("/{id}/return")
 public ResponseEntity<Transaction> returnBook(@PathVariable Long id) {
     return ResponseEntity.ok(transactionService.returnBook(id));
 }
 
 @GetMapping("/{id}/fine")
 public ResponseEntity<Double> calculateFine(@PathVariable Long id) {
     return ResponseEntity.ok(transactionService.calculateFine(id));
 }
 
 @PutMapping("/update-overdue")
 public ResponseEntity<Void> updateOverdueStatus() {
     transactionService.updateOverdueStatus();
     return ResponseEntity.ok().build();
 }
}
