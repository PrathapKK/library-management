
//src/test/java/com/library/management/service/UserServiceTest.java
package com.codehawk.library.librarymanagement.service;

import com.codehawk.library.librarymanagement.exception.DuplicateResourceException;
import com.codehawk.library.librarymanagement.exception.ResourceNotFoundException;
import com.codehawk.library.librarymanagement.model.Transaction;
import com.codehawk.library.librarymanagement.model.User;
import com.codehawk.library.librarymanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

 @Mock
 private UserRepository userRepository;

 @Mock
 private PasswordEncoder passwordEncoder;

 @InjectMocks
 private UserService userService;

 private User user;

 @BeforeEach
 void setUp() {
     user = new User();
     user.setId(1L);
     user.setName("Test User");
     user.setEmail("test@example.com");
     user.setPassword("password");
     user.setRole(User.Role.USER);
     user.setTransactions(new ArrayList<>());
 }

 @Test
 void shouldGetAllUsers() {
     // given
     List<User> users = Arrays.asList(user);
     given(userRepository.findAll()).willReturn(users);

     // when
     List<User> result = userService.getAllUsers();

     // then
     assertThat(result).isNotNull();
     assertThat(result.size()).isEqualTo(1);
     assertThat(result.get(0).getName()).isEqualTo("Test User");
 }

 @Test
 void shouldGetUserById() {
     // given
     given(userRepository.findById(1L)).willReturn(Optional.of(user));

     // when
     User result = userService.getUserById(1L);

     // then
     assertThat(result).isNotNull();
     assertThat(result.getId()).isEqualTo(1L);
 }

 @Test
 void shouldThrowExceptionWhenUserNotFound() {
     // given
     given(userRepository.findById(1L)).willReturn(Optional.empty());

     // when & then
     assertThrows(ResourceNotFoundException.class, () -> {
         userService.getUserById(1L);
     });
 }
 
 @Test
 void shouldGetUserByEmail() {
     // given
     given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));

     // when
     User result = userService.getUserByEmail("test@example.com");

     // then
     assertThat(result).isNotNull();
     assertThat(result.getEmail()).isEqualTo("test@example.com");
 }

 @Test
 void shouldThrowExceptionWhenUserNotFoundByEmail() {
     // given
     given(userRepository.findByEmail("test@example.com")).willReturn(Optional.empty());

     // when & then
     assertThrows(ResourceNotFoundException.class, () -> {
         userService.getUserByEmail("test@example.com");
     });
 }

 @Test
 void shouldRegisterUser() {
     // given
     given(userRepository.existsByEmail(anyString())).willReturn(false);
     given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
     given(userRepository.save(any(User.class))).willReturn(user);

     // when
     User savedUser = userService.registerUser(user);

     // then
     assertThat(savedUser).isNotNull();
     verify(userRepository, times(1)).save(any(User.class));
     verify(passwordEncoder, times(1)).encode(anyString());
 }

 @Test
 void shouldThrowExceptionWhenRegisteringUserWithExistingEmail() {
     // given
     given(userRepository.existsByEmail(anyString())).willReturn(true);

     // when & then
     assertThrows(DuplicateResourceException.class, () -> {
         userService.registerUser(user);
     });
     
     verify(userRepository, never()).save(any(User.class));
 }

 @Test
 void shouldUpdateUser() {
     // given
     User updatedUser = new User();
     updatedUser.setName("Updated Name");
     updatedUser.setEmail("test@example.com");
     updatedUser.setAddress("123 Test St");
     updatedUser.setPhone("1234567890");
     
     given(userRepository.findById(1L)).willReturn(Optional.of(user));
     given(userRepository.save(any(User.class))).willReturn(user);

     // when
     User result = userService.updateUser(1L, updatedUser);

     // then
     assertThat(result).isNotNull();
     verify(userRepository, times(1)).save(any(User.class));
 }

 @Test
 void shouldThrowExceptionWhenUpdatingUserWithExistingEmail() {
     // given
     User updatedUser = new User();
     updatedUser.setName("Updated Name");
     updatedUser.setEmail("new@example.com");
     
     given(userRepository.findById(1L)).willReturn(Optional.of(user));
     given(userRepository.existsByEmail("new@example.com")).willReturn(true);

     // when & then
     assertThrows(DuplicateResourceException.class, () -> {
         userService.updateUser(1L, updatedUser);
     });
     
     verify(userRepository, never()).save(any(User.class));
 }

 @Test
 void shouldDeleteUser() {
     // given
     given(userRepository.findById(1L)).willReturn(Optional.of(user));
     
     // when
     userService.deleteUser(1L);
     
     // then
     verify(userRepository, times(1)).delete(any(User.class));
 }
 
 @Test
 void shouldThrowExceptionWhenDeletingUserWithActiveTransactions() {
     // given
     User userWithTransactions = new User();
     userWithTransactions.setId(1L);
     List<Transaction> transactions = new ArrayList<>();
     transactions.add(new Transaction());
     userWithTransactions.setTransactions(transactions);
     
     given(userRepository.findById(1L)).willReturn(Optional.of(userWithTransactions));

     // when & then
     assertThrows(IllegalStateException.class, () -> {
         userService.deleteUser(1L);
     });
     
     verify(userRepository, never()).delete(any(User.class));
 }

 @Test
 void shouldChangePassword() {
     // given
     given(userRepository.findById(1L)).willReturn(Optional.of(user));
     given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
     given(passwordEncoder.encode(anyString())).willReturn("newEncodedPassword");
     given(userRepository.save(any(User.class))).willReturn(user);

     // when
     User result = userService.changePassword(1L, "oldPassword", "newPassword");

     // then
     assertThat(result).isNotNull();
     verify(passwordEncoder, times(1)).matches(anyString(), anyString());
     verify(passwordEncoder, times(1)).encode(anyString());
     verify(userRepository, times(1)).save(any(User.class));
 }

 @Test
 void shouldThrowExceptionWhenOldPasswordIsIncorrect() {
     // given
     given(userRepository.findById(1L)).willReturn(Optional.of(user));
     given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

     // when & then
     assertThrows(IllegalArgumentException.class, () -> {
         userService.changePassword(1L, "wrongOldPassword", "newPassword");
     });
     
     verify(passwordEncoder, never()).encode(anyString());
     verify(userRepository, never()).save(any(User.class));
 }
}