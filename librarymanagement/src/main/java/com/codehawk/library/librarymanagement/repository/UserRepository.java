
//src/main/java/com/library/management/repository/UserRepository.java
package com.codehawk.library.librarymanagement.repository;

import com.codehawk.library.librarymanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
 Optional<User> findByEmail(String email);
 boolean existsByEmail(String email);
}