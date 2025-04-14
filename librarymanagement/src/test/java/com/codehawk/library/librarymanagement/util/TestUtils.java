package com.codehawk.library.librarymanagement.util;

import com.codehawk.library.librarymanagement.model.Book;
import com.codehawk.library.librarymanagement.model.Transaction;
import com.codehawk.library.librarymanagement.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Utility class for testing purposes.
 * Provides helper methods to create test objects and manage test authentication.
 */
public class TestUtils {

    /**
     * Creates a test user with the given role
     */
    public static User createTestUser(String name, String email, User.Role role) {
        User user = new User();
        user.setId(100L); // Use a high ID to avoid conflicts with existing data
        user.setName(name);
        user.setEmail(email);
        user.setPassword("$2a$10$eDhNCmQES.hZgz0UqkDDmOh3FENZ9/ZDGdvmMft3BsWgsCdC5MnIi"); // encoded "password123"
        user.setRole(role);
        return user;
    }

    /**
     * Creates a test book
     */
    public static Book createTestBook(String title, String author, String isbn, int totalCopies) {
        Book book = new Book();
        book.setId(100L); // Use a high ID to avoid conflicts with existing data
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setPublisher("Test Publisher");
        book.setPublicationDate(LocalDate.now());
        book.setCategory("Test Category");
        book.setTotalCopies(totalCopies);
        book.setAvailableCopies(totalCopies);
        book.setAvailable(totalCopies > 0);
        return book;
    }

    /**
     * Creates a test transaction
     */
    public static Transaction createTestTransaction(User user, Book book, Transaction.Status status) {
        Transaction transaction = new Transaction();
        transaction.setId(100L); // Use a high ID to avoid conflicts with existing data
        transaction.setUser(user);
        transaction.setBook(book);
        transaction.setIssueDate(LocalDate.now());
        transaction.setDueDate(LocalDate.now().plusDays(14));
        transaction.setStatus(status);
        
        if (status == Transaction.Status.RETURNED) {
            transaction.setReturnDate(LocalDate.now());
        }
        
        return transaction;
    }

    /**
     * Generates a test JWT token for the given email and role
     */
    public static String generateTestJwtToken(String email, String role, String secretKey) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600000); // 1 hour

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    /**
     * Sets up security context for test with the specified role
     */
    public static void setSecurityContext(String username, String role) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        
        List<SimpleGrantedAuthority> authorities = 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
        
        Authentication authentication = 
                new UsernamePasswordAuthenticationToken(username, null, authorities);
        
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * Clears the security context after test
     */
    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}