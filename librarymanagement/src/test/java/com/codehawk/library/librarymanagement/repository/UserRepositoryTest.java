package com.codehawk.library.librarymanagement.repository;

import com.codehawk.library.librarymanagement.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindUserByEmail() {
        // given
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(User.Role.USER);
        
        entityManager.persist(user);
        entityManager.flush();

        // when
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test User");
    }

    @Test
    void shouldNotFindUserByNonExistingEmail() {
        // when
        Optional<User> found = userRepository.findByEmail("nonexisting@example.com");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldCheckIfEmailExists() {
        // given
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(User.Role.USER);
        
        entityManager.persist(user);
        entityManager.flush();

        // when
        boolean existingEmail = userRepository.existsByEmail("test@example.com");
        boolean nonExistingEmail = userRepository.existsByEmail("nonexisting@example.com");

        // then
        assertThat(existingEmail).isTrue();
        assertThat(nonExistingEmail).isFalse();
    }

    @Test
    void shouldFindAllUsers() {
        // given
        User user1 = new User();
        user1.setName("User 1");
        user1.setEmail("user1@example.com");
        user1.setPassword("password1");
        user1.setRole(User.Role.USER);
        
        User user2 = new User();
        user2.setName("User 2");
        user2.setEmail("user2@example.com");
        user2.setPassword("password2");
        user2.setRole(User.Role.LIBRARIAN);
        
        User user3 = new User();
        user3.setName("User 3");
        user3.setEmail("user3@example.com");
        user3.setPassword("password3");
        user3.setRole(User.Role.ADMIN);
        
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);
        entityManager.flush();

        // when
        List<User> users = userRepository.findAll();

        // then
        assertThat(users).hasSize(3);
        assertThat(users).extracting(User::getEmail)
                         .containsExactlyInAnyOrder("user1@example.com", "user2@example.com", "user3@example.com");
    }

    @Test
    void shouldSaveUser() {
        // given
        User user = new User();
        user.setName("New User");
        user.setEmail("new@example.com");
        user.setPassword("newpassword");
        user.setAddress("123 New St");
        user.setPhone("1234567890");
        user.setRole(User.Role.USER);

        // when
        User savedUser = userRepository.save(user);

        // then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("New User");
        assertThat(savedUser.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void shouldUpdateUser() {
        // given
        User user = new User();
        user.setName("Original Name");
        user.setEmail("original@example.com");
        user.setPassword("password");
        user.setRole(User.Role.USER);
        
        entityManager.persist(user);
        entityManager.flush();
        
        // when
        User foundUser = userRepository.findById(user.getId()).get();
        foundUser.setName("Updated Name");
        foundUser.setAddress("Updated Address");
        userRepository.save(foundUser);
        
        // then
        User updatedUser = userRepository.findById(user.getId()).get();
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getAddress()).isEqualTo("Updated Address");
        assertThat(updatedUser.getEmail()).isEqualTo("original@example.com"); // Email remains the same
    }

    @Test
    void shouldDeleteUser() {
        // given
        User user = new User();
        user.setName("User to Delete");
        user.setEmail("delete@example.com");
        user.setPassword("password");
        user.setRole(User.Role.USER);
        
        entityManager.persist(user);
        entityManager.flush();
        
        // when
        userRepository.deleteById(user.getId());
        
        // then
        Optional<User> deletedUser = userRepository.findById(user.getId());
        assertThat(deletedUser).isEmpty();
    }

    @Test
    void shouldReturnUsersByRole() {
        // given
        User user1 = new User();
        user1.setName("Regular User 1");
        user1.setEmail("user1@example.com");
        user1.setPassword("password1");
        user1.setRole(User.Role.USER);
        
        User user2 = new User();
        user2.setName("Regular User 2");
        user2.setEmail("user2@example.com");
        user2.setPassword("password2");
        user2.setRole(User.Role.USER);
        
        User librarian = new User();
        librarian.setName("Librarian");
        librarian.setEmail("librarian@example.com");
        librarian.setPassword("password3");
        librarian.setRole(User.Role.LIBRARIAN);
        
        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin@example.com");
        admin.setPassword("password4");
        admin.setRole(User.Role.ADMIN);
        
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(librarian);
        entityManager.persist(admin);
        entityManager.flush();

        // Create a custom query method for testing if not already in your repository
        List<User> regularUsers = entityManager.getEntityManager()
            .createQuery("SELECT u FROM User u WHERE u.role = :role", User.class)
            .setParameter("role", User.Role.USER)
            .getResultList();
        
        // then
        assertThat(regularUsers).hasSize(2);
        assertThat(regularUsers).extracting(User::getName)
                               .containsExactlyInAnyOrder("Regular User 1", "Regular User 2");
    }
}