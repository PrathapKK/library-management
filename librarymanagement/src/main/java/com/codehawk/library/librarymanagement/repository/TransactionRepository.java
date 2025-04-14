
//src/main/java/com/library/management/repository/TransactionRepository.java
package com.codehawk.library.librarymanagement.repository;

import com.codehawk.library.librarymanagement.model.Transaction;
import com.codehawk.library.librarymanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
 List<Transaction> findByUser(User user);
 List<Transaction> findByUserAndStatus(User user, Transaction.Status status);
 List<Transaction> findByDueDateBeforeAndStatus(LocalDate date, Transaction.Status status);
}
