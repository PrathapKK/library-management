
//src/main/java/com/library/management/model/Transaction.java
package com.codehawk.library.librarymanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 
 @ManyToOne
 @JoinColumn(name = "book_id", nullable = false)
 private Book book;
 
 @ManyToOne
 @JoinColumn(name = "user_id", nullable = false)
 private User user;
 
 @Column(nullable = false)
 private LocalDate issueDate;
 
 private LocalDate dueDate;
 
 private LocalDate returnDate;
 
 @Enumerated(EnumType.STRING)
 private Status status;
 
 private Double fine;
 
 public enum Status {
     REQUESTED, ISSUED, RETURNED, OVERDUE, LOST
 }
}